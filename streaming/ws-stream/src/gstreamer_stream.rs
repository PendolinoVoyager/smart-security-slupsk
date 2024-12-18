use std::time::Duration;

use anyhow::Result;

pub struct GStreamerLibcameraStream {
    audio_dev: Option<String>,
    rx: std::sync::mpsc::Receiver<Vec<u8>>,
    appsink: gstreamer_app::AppSink,
}

impl GStreamerLibcameraStream {
    /// Create and initialize a gstreamer stream.
    pub fn init(config: &crate::Config) -> Result<Self> {
        // Initialize GStreamer
        gstreamer::init()?;

        todo!();
        // Return the stream
        // Ok(Self {
        //     audio_dev: config.audio_device.clone(),
        //     pipeline,
        //     appsink,
        // })
    }
}

impl crate::stream::VideoStream for GStreamerLibcameraStream {
    fn read(&mut self, buf: &mut [u8]) -> std::io::Result<usize> {
        self.rx
            .recv_timeout(Duration::from_millis(16))
            .or_else(|_| Ok(0))
    }
}
