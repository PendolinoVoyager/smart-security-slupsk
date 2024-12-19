use crate::ffmpeg_stream::FfmpegMpegtsStream;
use crate::gstreamer_stream::GStreamerLibcameraStream;
pub trait VideoStream {
    fn read(&mut self, buf: &mut [u8]) -> std::io::Result<usize>;
    fn start(&mut self);
    fn stop(&mut self);
    fn stream_state(&self) -> StreamState;
}
#[allow(unused)]
pub enum StreamKind {
    FfmpegMpegtsStream(FfmpegMpegtsStream),
    GStreamerLibcameraStream(GStreamerLibcameraStream),
}

pub enum StreamState {
    Idle,
    Running,
    Errored,
}
