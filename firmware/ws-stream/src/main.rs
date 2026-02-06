#![feature(type_changing_struct_update)]
#![feature(stmt_expr_attributes)]

//! Binary utility to stream to a websocket.
mod buffer;
mod config;

use std::net::TcpStream;
use std::time::Duration;

use buffer::StreamBuffer;
use clap::Parser;
use config::Config;

use tracing::Level;
use tracing_subscriber::FmtSubscriber;
use tungstenite::stream::MaybeTlsStream;
use tungstenite::{Bytes, Message};
use rustls::crypto::{CryptoProvider, ring};

/// This is the source from the init stream shell script.
const STREAM_SOURCE: &str = "127.0.0.1:10001";
/// Timeout to when no new stream parts arrive.
/// If it exceeds this time, the client disconnects because the pipeline broke at some point.
const STREAM_TIMEOUT: Duration = Duration::from_secs(5);

const TOKEN_FILE: &str = "/etc/sss_firmware/token.txt";

fn main() {
    CryptoProvider::install_default(ring::default_provider())
        .expect("install rustls crypto provider");
    let mut config = config::Config::parse();
    if !config.silent {
        let subscriber = FmtSubscriber::builder()
            .with_writer(std::io::stderr) // Write logs to stderr
            .with_max_level(Level::TRACE) // Set the maximum level of logs (optional)
            .finish();
        tracing::subscriber::set_global_default(subscriber)
            .expect("Setting default subscriber failed");
    }
    if config.token.is_none() {
        config.token = match std::fs::read_to_string(TOKEN_FILE).ok() {
            Some(t) => {
                let t = t.trim().to_owned();
                Some(t)
            }
            None => {
                tracing::error!("Failure to read token! Device may not be configured");
                return;
            }
        }
    }
    let _ = connect_ws(config).inspect_err(|e| tracing::error!("Stream resulted in failure: {e}"));
}

/// Make an connection to a websocket server and stream directly to it.
fn connect_ws(config: Config) -> anyhow::Result<()> {
    let addr = format!(
        "{}/streaming-server/v1/ws/device_checkout?token={}",
        &config.addr,
        &config.token.expect("At this point token should be present")
    );
    tracing::info!("Attempting to connect to {}", &addr);

    let (ws, res) = tungstenite::connect(&addr)?;

    tracing::info!("WebSocket created");
    tracing::debug!("{res:?}");
    stream_until_disconnect(ws);

    Ok(())
}

fn stream_until_disconnect(mut ws: tungstenite::WebSocket<MaybeTlsStream<TcpStream>>) {
    tracing::info!("Initializing stream");
    let stream_socket = std::net::UdpSocket::bind(STREAM_SOURCE)
        .expect("Cannot bind to the socket with the stream.");
    let _ = stream_socket.set_read_timeout(Some(STREAM_TIMEOUT));
    tracing::info!("Starting streaming...");

    let mut buffer = StreamBuffer::new(192 * 10, stream_socket);
    tracing::info!("Created buffer...");

    while let Ok(read) = buffer.read() {
        tracing::info!("{}", read.len());
        let msg = Message::Binary(Bytes::copy_from_slice(read));
        if let Err(e) = ws.send(msg) {
            tracing::warn!("Cannot send packet, connection broken!");
            tracing::error!("{e}");
            break;
        }
    }

    tracing::warn!("Stream unexpectedly ended.");
}
