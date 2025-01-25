//! Module containing Clap definition of app config.
use clap::Parser;

/// The UDP address that the process responsible for machine vision purposes will use
pub const MACHINE_VISION_PROCESS_ADDR: &str = "127.0.0.1:10000";

#[derive(Parser, Debug)]
#[command(version, about, long_about = None)]
/// ffmpeg based utility to stream video via WebSockets
/// Requires ffmpeg, v4l2, and gstreamer.
/// Streams using MPEGTS.
/// The stream is also piped to localhost UDP.
pub(crate) struct Config {
    /// IP address to stream to: e.g. "192.168.1.10"
    #[arg(long, short)]
    pub addr: String,

    /// Disable audio for the stream.
    /// If not present, alsa will be used.
    /// When audio cannot be produced, warnings will be emitted.
    #[clap(long = "noaudio", action, default_value_t = false)]
    pub no_audio: bool,

    /// Do not log
    #[clap(long, action, default_value_t = false)]
    pub silent: bool,
    /// Specify the stream kind. Available options
    /// - libcamera (default)
    /// - v4l2
    #[clap(long, default_value_t = String::from("libcamera"))]
    pub streamkind: String,

    /// Do not wait for START / STOP commands, just send the stream
    #[clap(long, default_value_t = false)]
    pub raw: bool,
}
