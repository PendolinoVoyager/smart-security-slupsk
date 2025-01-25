use super::libcamera_ha_stream::GStreamerLibcameraStream;
use super::VideoStream;
use crate::stream::v4l2_stream::GstreamerV4L2Stream;

// all elements implement VideoStream
pub enum StreamKind {
    V4L2GstreamerStream(GstreamerV4L2Stream),
    GStreamerLibcameraStream(GStreamerLibcameraStream),
}

/// TODO! This sucks
impl VideoStream for StreamKind {
    fn read(&mut self, buf: &mut [u8]) -> Result<usize, crate::stream::StreamReadError> {
        match self {
            StreamKind::V4L2GstreamerStream(s) => s.read(buf),
            StreamKind::GStreamerLibcameraStream(s) => s.read(buf),
        }
    }

    fn start(&mut self) {
        match self {
            StreamKind::V4L2GstreamerStream(s) => s.start(),
            StreamKind::GStreamerLibcameraStream(s) => s.start(),
        }
    }

    fn stop(&mut self) {
        match self {
            StreamKind::V4L2GstreamerStream(s) => s.stop(),
            StreamKind::GStreamerLibcameraStream(s) => s.stop(),
        }
    }

    fn stream_state(&self) -> crate::stream::StreamState {
        match self {
            StreamKind::V4L2GstreamerStream(s) => s.stream_state(),
            StreamKind::GStreamerLibcameraStream(s) => s.stream_state(),
        }
    }
}
pub fn create_stream(config: &crate::config::Config) -> anyhow::Result<StreamKind> {
    match config.streamkind.as_str() {
        "libcamera" => Ok(StreamKind::GStreamerLibcameraStream(
            GStreamerLibcameraStream::init(config)?,
        )),
        "v4l2" => Ok(StreamKind::V4L2GstreamerStream(GstreamerV4L2Stream::init(
            config,
        )?)),
        s => Err(anyhow::Error::msg(format!("Unknown stream type {s}"))),
    }
}
