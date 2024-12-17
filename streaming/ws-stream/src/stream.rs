use crate::ffmpeg_stream::{FfmpegMpegtsStream, InitStateOk};

pub trait VideoStream {
    fn read(&mut self, buf: &mut [u8]) -> std::io::Result<usize>;
}

pub enum StreamKind {
    FfmpegMpegtsStream(FfmpegMpegtsStream<InitStateOk>),
}
