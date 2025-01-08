use super::ffmpeg_stream::FfmpegMpegtsStream;
use super::gstreamer_stream::GStreamerLibcameraStream;
use crate::stream::VideoStream;

// all elements implement VideoStream
pub enum StreamKind {
    FfmpegMpegtsStream(FfmpegMpegtsStream),
    GStreamerLibcameraStream(GStreamerLibcameraStream),
}

/// TODO! This sucks
impl VideoStream for StreamKind {
    fn read(&mut self, buf: &mut [u8]) -> Result<usize, crate::stream::StreamReadError> {
        match self {
            StreamKind::FfmpegMpegtsStream(s) => s.read(buf),
            StreamKind::GStreamerLibcameraStream(s) => s.read(buf),
        }
    }

    fn start(&mut self) {
        match self {
            StreamKind::FfmpegMpegtsStream(s) => s.start(),
            StreamKind::GStreamerLibcameraStream(s) => s.start(),
        }
    }

    fn stop(&mut self) {
        match self {
            StreamKind::FfmpegMpegtsStream(s) => s.stop(),
            StreamKind::GStreamerLibcameraStream(s) => s.stop(),
        }
    }

    fn stream_state(&self) -> crate::stream::StreamState {
        match self {
            StreamKind::FfmpegMpegtsStream(s) => s.stream_state(),
            StreamKind::GStreamerLibcameraStream(s) => s.stream_state(),
        }
    }
}
pub fn create_stream(config: &crate::config::Config) -> anyhow::Result<StreamKind> {
    match config.streamkind.as_str() {
        "gstreamer" => Ok(StreamKind::GStreamerLibcameraStream(
            GStreamerLibcameraStream::init(config)?,
        )),
        "ffmpeg" => Ok(StreamKind::FfmpegMpegtsStream(FfmpegMpegtsStream::new(
            config,
        ))),
        s => Err(anyhow::Error::msg(format!("Unknown stream type {s}"))),
    }
}
