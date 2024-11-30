//! This module is responsible for providing an API for a MP4 stream.
//! It might use ffmpeg but that may change in the future.

use std::io::Read;
use std::process::{Child, ChildStdout, Stdio};

/// Wrapper for a mp4 stream
pub struct Mp4Stream {
    /// Video device currently used. Defaults to "/dev/video0"
    pub video_dev: String,
    /// Audio device currently used
    pub audio_dev: Option<String>,
    child: Option<Child>,
}

#[allow(unused)]
impl Mp4Stream {
    pub fn new(video_dev: Option<String>, audio_dev: Option<String>) -> Self {
        Self {
            child: None,
            video_dev: video_dev.unwrap_or("/dev/video0".into()),
            audio_dev,
        }
    }
    /// Init the stream to get the source of packets.
    pub fn init(&mut self) {
        let mut command = std::process::Command::new("ffmpeg");

        if let Some(audio_dev) = &self.audio_dev {
            command
                .arg("-f")
                .arg("pulse")
                .arg("-i")
                .arg(audio_dev)
                .arg("-c:a")
                .arg("libopus")
                .arg("-ac")
                .arg("2");
        }

        let child = command
            .arg("-fflags")
            .arg("nobuffer")
            .arg("-f")
            .arg("v4l2")
            .arg("-i")
            .arg(&self.video_dev)
            .arg("-c:v")
            .arg("libvpx") // Use VP8 for video encoding
            .arg("-g")
            .arg("30") // Keyframe interval (GOP size), adjust for smoother video
            .arg("-f")
            .arg("webm") // Output format WebM
            .arg("-deadline")
            .arg("realtime") // Low-latency encoding
            .arg("-cpu-used")
            .arg("3") // Leave one core alone (performance optimization)
            .arg("-cluster_time_limit")
            .arg("300") // Cluster time limit to prevent stream fragmentation
            .arg("-")
            .stdin(Stdio::null())
            .stdout(Stdio::piped())
            .stderr(Stdio::piped())
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
    /// Take error from stderr (if applicable)
    pub fn error(&mut self) -> Option<String> {
        if let Some(mut child) = self.child.take() {
            if let Some(mut stderr) = child.stderr {
                let mut str = String::new();
                stderr.read_to_string(&mut str);
                return Some(str);
            }
        }
        None
    }
}
impl Drop for Mp4Stream {
    fn drop(&mut self) {
        if let Some(p) = &mut self.child {
            let _ = p.kill();
        }
    }
}
