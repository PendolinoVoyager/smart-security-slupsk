#![feature(type_changing_struct_update)]
#![feature(let_chains)]
//! Binary utility to stream to a websocket.
mod buffer;
mod config;

use std::time::Duration;

use buffer::StreamBuffer;
use clap::Parser;
use config::Config;
use futures::{SinkExt, Stream};

use tokio::net::TcpStream;
use tokio_tungstenite::tungstenite::Message;
use tokio_tungstenite::MaybeTlsStream;
use tokio_util::bytes::Bytes;
use tracing::Level;
use tracing_subscriber::FmtSubscriber;

const STREAM_SOURCE: &str = "127.0.0.1:5001";
const STREAM_TIMEOUT: Duration = Duration::from_secs(5);

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    let config = config::Config::parse();
    if !config.silent {
        let subscriber = FmtSubscriber::builder()
            .with_writer(std::io::stderr) // Write logs to stderr
            .with_max_level(Level::INFO) // Set the maximum level of logs (optional)
            .finish();
        tracing::subscriber::set_global_default(subscriber)
            .expect("Setting default subscriber failed");
    }

    connect_ws(config)
        .await
        .inspect_err(|e| tracing::error!("Stream resulted in failure: {e}"))?;

    Ok(())
}

/// Make an connection to a websocket server and stream.
async fn connect_ws(config: Config) -> anyhow::Result<()> {
    tracing::info!("Attempting to connect to {}", &config.addr);

    let (ws, res) = tokio_tungstenite::connect_async(&config.addr).await?;

    tracing::info!("WebSocket created");
    tracing::debug!("{res:?}");
    stream_until_disconnect(ws).await;

    Ok(())
}

async fn stream_until_disconnect(
    mut ws: tokio_tungstenite::WebSocketStream<MaybeTlsStream<TcpStream>>,
) {
    tracing::info!("Initializing stream");
    let stream_socket = std::net::UdpSocket::bind(STREAM_SOURCE)
        .expect("Cannot bind to the socket with the stream.");

    let _ = stream_socket.set_read_timeout(Some(STREAM_TIMEOUT));
    tracing::info!("Starting streaming...");
    let mut buffer = StreamBuffer::new(192 * 10, stream_socket);
    while let Ok(read) = buffer.read() {
        tracing::info!("{}", read.len());
        let msg = Message::Binary(Bytes::copy_from_slice(read));
        if let Err(e) = ws.send(msg).await {
            tracing::warn!("Cannot send packet, connection broken!");
            tracing::error!("{e}");
            break;
        }
    }

    tracing::info!("Stream ended");
}
