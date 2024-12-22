#![feature(type_changing_struct_update)]
#![feature(let_chains)]
//! Binary utility to stream to a websocket.
mod audio;
mod ffmpeg_stream;
mod gstreamer_stream;
mod stream;
mod stream_factory;

use clap::Parser;
use std::io::{self};
use std::net::{IpAddr, Ipv4Addr, SocketAddr, TcpListener, TcpStream};
use stream::{StreamBuffer, VideoStream};
use tracing::Level;
use tracing_subscriber::FmtSubscriber;
use tungstenite::{accept_hdr, Message, WebSocket};
#[derive(Parser, Debug)]
#[command(version, about, long_about = None)]
/// ffmpeg based utility to stream video via WebSockets
/// Requires ffmpeg, v4l2, and gstreamer.
/// Streams using MPEGTS.
struct Config {
    /// IP address to stream to: e.g. "192.168.1.10"
    #[arg(long, short)]
    addr: String,

    /// Port to bind the streaming WebSocket to.
    #[arg(short, long, default_value_t = 0)]
    port: u16,

    /// Wait for a connection from the provided address
    #[clap(long, action, default_value_t = false)]
    server: bool,
    /// Disable audio for the stream.
    /// If not present, alsa will be used.
    /// When audio cannot be produced, warnings will be emitted.
    #[clap(long = "noaudio", action, default_value_t = false)]
    no_audio: bool,

    /// Do not log
    #[clap(long, action, default_value_t = false)]
    silent: bool,
    /// Specify the stream kind. Available options
    /// - gstreamer (default)
    /// - ffmpeg
    #[clap(long, default_value_t = String::from("gstreamer"))]
    streamkind: String,
}

fn main() -> anyhow::Result<()> {
    let config = Config::parse();
    if !config.silent {
        let subscriber = FmtSubscriber::builder()
            .with_writer(io::stderr) // Write logs to stderr
            .with_max_level(Level::INFO) // Set the maximum level of logs (optional)
            .finish();
        tracing::subscriber::set_global_default(subscriber)
            .expect("Setting default subscriber failed");
    }

    if config.server {
        serve_ws(config)?;
    } else {
        connect_ws(config)?;
    }
    Ok(())
}

/// Make a websocket server, listening for the connection and then responding with stream.
fn serve_ws(config: Config) -> anyhow::Result<()> {
    let addr = match config.addr == "127.0.0.1" {
        true => IpAddr::V4(Ipv4Addr::LOCALHOST),
        false => get_if_addr().ok_or(std::io::Error::new(
            io::ErrorKind::AddrNotAvailable,
            "Public address not available",
        ))?,
    };
    let sock_addr = SocketAddr::new(addr, config.port);

    tracing::info!("Attempting to bind the socket to {}", &sock_addr);
    let listener: TcpListener = TcpListener::bind(sock_addr)?;
    tracing::info!("WebSocket server bound to {}!", &sock_addr);

    let callback = |req: &tungstenite::http::Request<()>,
                    response: tungstenite::http::Response<_>| {
        tracing::info!("Request URI: {}", req.uri()); // Get the URI
        tracing::info!("Headers: {:?}", req.headers()); // Log headers
        Ok(response)
    };

    while let Ok((stream, addr)) = listener.accept() {
        if addr.ip().to_string() != config.addr {
            let _ = stream.shutdown(std::net::Shutdown::Both);
            continue;
        }
        drop(listener);
        tracing::info!("Connected to remote host");

        match accept_hdr(stream, callback) {
            Ok(ws) => {
                stream_until_disconnect(ws, config);
            }
            Err(e) => {
                tracing::error!("Bad WebSocket handshake with {}", addr.ip());
                tracing::error!("{:?}", e);
            }
        }

        break;
    }
    Ok(())
}
/// Make an connection to a websocket server and stream.
fn connect_ws(config: Config) -> anyhow::Result<()> {
    let addr = get_if_addr().ok_or(std::io::Error::new(
        io::ErrorKind::AddrNotAvailable,
        "Public address not available",
    ))?;
    let sock_addr = SocketAddr::new(addr, config.port);
    tracing::info!("Attempting to connect to {}", &sock_addr);
    let tcp_stream = TcpStream::connect(sock_addr)?;
    tracing::info!("TcpConnection established with {}!", &sock_addr);
    let ws = tungstenite::accept(tcp_stream)?;
    tracing::info!("WebSocket created");
    stream_until_disconnect(ws, config);

    Ok(())
}

fn stream_until_disconnect(mut ws: WebSocket<TcpStream>, config: Config) {
    tracing::info!("Initializing stream");
    let mut stream = match stream_factory::create_stream(&config) {
        Err(e) => {
            tracing::error!("{e}");
            return;
        }
        Ok(s) => s,
    };

    stream.start();
    tracing::info!("Starting streaming...");
    if !ws.can_write() {
        tracing::error!("Cannot write to the websocket!");
    }
    let mut reader = StreamBuffer::new(1024, stream);
    while let Ok(buf) = reader.read() {
        tracing::info!("{}", buf.len());
        let message = Message::Binary(buf.to_vec());
        if ws.send(message).is_err() {
            tracing::warn!("Cannot send packet, connection broken!");
            break;
        }
    }

    tracing::info!("Stream ended");
}

/// Helper function to get the address from the first non-loopback interface
fn get_if_addr() -> Option<IpAddr> {
    tracing::info!("Looking for public interface...");

    let ifaces = getifaddrs::getifaddrs().ok()?;

    // Iterate over the interfaces and look for one that isn't a loopback.
    for iface in ifaces {
        if iface.address.is_loopback() {
            continue;
        }

        return Some(iface.address);
    }

    None
}
