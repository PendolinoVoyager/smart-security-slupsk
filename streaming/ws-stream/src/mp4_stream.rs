//! This module is responsible for providing an API for a MP4 stream.
//! It might use ffmpeg but that may change in the future.

use std::process::{Child, ChildStdout, Stdio};

/// Wrapper for a mp4 stream
pub struct Mp4Stream {
    child: Option<Child>,
}

#[allow(unused)]
impl Mp4Stream {
    pub fn new() -> Self {
        Self { child: None }
    }
    /// Init the stream to get the source of packets.
    pub fn init(&mut self) {
        let child = std::process::Command::new("ffmpeg")
            .arg("-fflags")
            .arg("nobuffer")
            .arg("-f")
            .arg("v4l2")
            .arg("-i")
            .arg("/dev/video0")
            .arg("-c:v")
            .arg("libvpx") // Use H.264 codec
            .arg("-deadline")
            .arg("realtime")
            .arg("-cpu-used")
            .arg("3") // leave one core alone
            .arg("-f")
            .arg("webm")
            .arg("-g")
            .arg("30")
            .arg("-cluster_time_limit")
            .arg("300")
            .arg("-") // Output to stdout (you could change this if you want a file instead)
            .stdin(Stdio::null()) // No need to provide stdin for ffmpeg
            .stdout(Stdio::piped())
            .stderr(Stdio::null())
            .spawn()
            .expect("Failed to spawn ffmpeg process");

        self.child = Some(child);
    }
    /// Stop the webcam stream. Receiving the packets will do nothing.
    pub fn stop(&mut self) {
        if let Some(mut child) = self.child.take() {
            let _ = child.kill();
        }
    }
    pub fn output(&mut self) -> Option<ChildStdout> {
        if let Some(child) = &mut self.child {
            child.stdout.take()
        } else {
            None
        }
    }
}
impl Drop for Mp4Stream {
    fn drop(&mut self) {
        if let Some(p) = &mut self.child {
            let _ = p.kill();
        }
    }
}
