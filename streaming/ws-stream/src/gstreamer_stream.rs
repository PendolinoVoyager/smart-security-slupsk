pub struct GStreamerLibcameraStream {
    audio_dev: Option<String>,
}

impl GStreamerLibcameraStream {
    pub fn new(audio_dev: Option<String>) -> Self {
        Self { audio_dev }
    }
    pub fn init() {}
}

impl crate::stream::VideoStream for GStreamerLibcameraStream {
    fn read(&mut self, buf: &mut [u8]) -> std::io::Result<usize> {
        todo!()
    }
}
