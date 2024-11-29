//! Binary utility to stream to a websocket.

mod mp4_stream;

use std::io::{self, Read};
use std::net::{IpAddr, SocketAddr, TcpListener, TcpStream, UdpSocket};

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
struct Args {
    ///  IP address to stream to: e.g. "192.168.1.10"
    #[arg(short, long)]
    addr_accept: String,

    /// Port to bind the streaming websocket to
    #[arg(short, long, default_value_t = 8080)]
    port: u16,

    /// Audio off
    #[clap(long, short, action, default_value_t = true)]
    no_audio: bool,
}

fn main() -> anyhow::Result<()> {
    let subscriber = FmtSubscriber::builder()
        .with_writer(io::stderr) // Write logs to stderr
        .with_max_level(Level::INFO) // Set the maximum level of logs (optional)
        .finish();

    // Set the global default subscriber
    tracing::subscriber::set_global_default(subscriber).expect("Setting default subscriber failed");

    let args = Args::parse();
    let addr = get_if_addr().ok_or(std::io::Error::new(
        io::ErrorKind::AddrNotAvailable,
        "Public address not available",
    ))?;
    let sock_addr = SocketAddr::new(addr, args.port);
    tracing::info!("Attempting to bind the socket to {}", &sock_addr);
    let listener: TcpListener = TcpListener::bind(sock_addr)?;
    tracing::info!("WebSocket server bound to {}!", &sock_addr);

    while let Ok((stream, addr)) = listener.accept() {
        if addr.ip().to_string() != args.addr_accept {
            let _ = stream.shutdown(std::net::Shutdown::Both);
            continue;
        }
        tracing::info!("Connected to remote host");
        let ws = tungstenite::accept(stream)?;
        stream_until_disconnect(ws);

        break;
    }
    // test_udp_stream();
    Ok(())
}

fn stream_until_disconnect(mut ws: WebSocket<TcpStream>) {
    let mut buf: Box<[u8; BUF_SIZE]> = Box::new([0; BUF_SIZE]);

    tracing::info!("Initializing stream");
    let mut stream = Mp4Stream::new();
    stream.init();
    let mut output = stream.output().unwrap();

    tracing::info!("Starting streaming...");
    if !ws.can_write() {
        tracing::error!("Cannot write to the websocket!");
    }
    while let Ok(len) = output.read(buf.as_mut_slice()) {
        tracing::info!("{len}");
        let message = Message::Binary(buf[0..len].to_vec());
        if ws.send(message).is_err() {
            tracing::warn!("Cannot send packet, connection broken!");
            break;
        }
    }
    tracing::info!("Stream ended");
    stream.stop();
}

fn test_udp_stream() {
    let mut buf: Box<[u8; BUF_SIZE]> = Box::new([0; BUF_SIZE]);
    let mut stream = Mp4Stream::new();
    stream.init();
    let udp = UdpSocket::bind("127.0.0.1:0").unwrap();
    let mut out = stream.output().unwrap();
    while let Ok(len) = out.read(buf.as_mut_slice()) {
        eprintln!("{len}");
        let _ = udp.send_to(&buf[0..len], "127.0.0.1:8000");
    }
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
