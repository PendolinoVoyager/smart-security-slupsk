//! Module containing Clap definition of app config.
use clap::Parser;

#[derive(Parser, Debug)]
#[command(version, about, long_about = None)]
/// ffmpeg based utility to stream video via WebSockets
/// Requires ffmpeg, v4l2, and gstreamer.
/// Streams using MPEGTS.
/// The stream is also piped to localhost UDP.
pub(crate) struct Config {
    /// IP address or URI to stream to: e.g. "192.168.1.10", "ws://12.12.32.123:8000"
    #[arg(long, short)]
    pub addr: String,

    /// Do not log
    #[clap(long, action, default_value_t = false)]
    pub silent: bool,

    /// Token override. If specified, it will use the provided device token.
    /// Otherwise, will try to read from /etc/sss_firmware/token.txt
    #[clap(long, short)]
    pub token: Option<String>,
}
