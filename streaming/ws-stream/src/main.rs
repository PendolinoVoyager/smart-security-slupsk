//! Binary utility to stream to a websocket.

mod mp4_stream;

use std::io::{self, Read};
use std::net::{IpAddr, SocketAddr, TcpListener, TcpStream};

use clap::Parser;
use mp4_stream::Mp4Stream;
use tracing::Level;
use tracing_subscriber::FmtSubscriber;
use tungstenite::{Message, WebSocket};

/// Buffer size for the stream.
/// It must be enough to fully encompass a packet
const BUF_SIZE: usize = 1024 * 100;

#[derive(Parser, Debug)]
#[command(version, about, long_about = None)]
/// ffmpeg based utility to stream video via WebSockets
/// Requires ffmpeg, v4l2 and OpenSSL.
/// Streams using WebM, vp8 and Vorbis.
struct Config {
    /// IP address to stream to: e.g. "192.168.1.10"
    #[arg(long, long)]
    addr: String,

    /// Port to bind the streaming websocket to
    #[arg(short, long, default_value_t = 0)]
    port: u16,

    /// wait for a connection from the provided address
    #[clap(long, action, default_value_t = false)]
    server: bool,
    /// Audio device to stream from
    #[clap(long, short)]
    audio_device: Option<String>,
    /// Video device. Defaults to /dev/video0
    #[clap(long, short, default_value_t = String::from("/dev/video0"))]
    video_device: String,

    /// Do not log
    #[clap(long, action, default_value_t = false)]
    silent: bool,
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
        serve_ws(config)
    } else {
        connect_ws(config)
    }
}

/// Make a websocket server, listening for the connection and then responding with stream.
fn serve_ws(config: Config) -> anyhow::Result<()> {
    let addr = get_if_addr().ok_or(std::io::Error::new(
        io::ErrorKind::AddrNotAvailable,
        "Public address not available",
    ))?;
    let sock_addr = SocketAddr::new(addr, config.port);

    tracing::info!("Attempting to bind the socket to {}", &sock_addr);
    let listener: TcpListener = TcpListener::bind(sock_addr)?;
    tracing::info!("WebSocket server bound to {}!", &sock_addr);

    while let Ok((stream, addr)) = listener.accept() {
        if addr.ip().to_string() != config.addr {
            let _ = stream.shutdown(std::net::Shutdown::Both);
            continue;
        }
        drop(listener);
        tracing::info!("Connected to remote host");
        let ws = tungstenite::accept(stream)?;
        stream_until_disconnect(ws, config);

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
    let mut buf: Box<[u8; BUF_SIZE]> = Box::new([0; BUF_SIZE]);

    tracing::info!("Initializing stream");
    let mut stream = Mp4Stream::new(Some(config.video_device), config.audio_device);
    stream.init();
    let mut output = stream.output().unwrap();

    tracing::info!("Starting streaming...");
    if !ws.can_write() {
        tracing::error!("Cannot write to the websocket!");
    }
    while let Ok(len) = output.read(buf.as_mut_slice()) {
        tracing::info!("{len}");
        if len == 0 {
            tracing::error!("ffmpeg pipe broken, exiting...");
            if let Some(e) = stream.error() {
                tracing::error!(e)
            }
            break;
        }
        let message = Message::Binary(buf[0..len].to_vec());
        if ws.send(message).is_err() {
            tracing::warn!("Cannot send packet, connection broken!");
            break;
        }
    }
    tracing::info!("Stream ended");
    stream.stop();
}

/// Helper function to get the address from the first non-loopback interface
fn get_if_addr() -> Option<IpAddr> {
    tracing::info!("Looking for public interface...");

    let ifaces = getifaddrs::getifaddrs().ok()?;

    // Iterate over the interfaces and look for one that isn't a loopback.
    for iface in ifaces {
        if iface.address.is_loopback() {
            continue; // Skip loopback interfaces
        }

        // Return the first non-loopback interface's address
        return Some(iface.address);
    }

    None // No suitable interface found
}
