//! This module is responsible for providing an API for a MP4 stream.
//! It might use ffmpeg but that may change in the future.

use std::io::Read;
use std::process::{Child, ChildStdout, Stdio};

use crate::stream::{StreamState, VideoStream};

/// Wrapper for a mp4 stream
pub struct FfmpegMpegtsStream {
    /// Video device currently used. Defaults to "/dev/video0"
    pub video_dev: String,
    /// Audio device currently used
    pub audio_dev: Option<String>,
    child: Option<Child>,
    stdout: Option<ChildStdout>,
}

// General implementation
impl FfmpegMpegtsStream {
    pub fn new(video_dev: Option<String>, audio_dev: Option<String>) -> Self {
        Self {
            child: None,
            video_dev: video_dev.unwrap_or("/dev/video0".into()),
            audio_dev,
            stdout: None,
        }
    }
    /// Init the stream to get the source of packets.
    fn init(&mut self) {
        let mut child = self.spawn_ffmpeg_command();
        match child.stdout.take() {
            Some(stdout) => self.stdout = Some(stdout),
            None => {
                tracing::error!("Cannot get child stdout! Possible ffmpeg init failed!");
                panic!()
            }
        };
        self.child = Some(child);
    }
    fn spawn_ffmpeg_command(&self) -> Child {
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
                .arg("1");
        }

        if let Some(audio_dev) = &self.audio_dev {
            command
                .arg("-fflags")
                .arg("nobuffer")
                .arg("-f")
                .arg("pulse")
                .arg("-i")
                .arg(audio_dev)
                .arg("-c:a")
                .arg("libopus")
                .arg("-ac")
                .arg("1");
        }

        command
            .arg("-fflags")
            .arg("nobuffer")
            .arg("-f")
            .arg("v4l2")
            .arg("-i")
            .arg(&self.video_dev)
            .arg("-c:v")
            .arg("libx264")
            .arg("-profile:v")
            .arg("baseline") // Use Main profile instead of High422
            .arg("-pix_fmt")
            .arg("yuv420p") // Convert chroma to 4:2:0 (yuv420p)
            .arg("-preset")
            .arg("ultrafast")
            .arg("-tune")
            .arg("zerolatency")
            .arg("-movflags")
            .arg("faststart+frag_keyframe+empty_moov")
            .arg("-frag_duration")
            .arg("500000")
            .arg("-f")
            .arg("mpegts")
            .arg("-")
            .stdin(Stdio::null())
            .stdout(Stdio::piped())
            .stderr(Stdio::piped())
            .spawn()
            .expect("Failed to spawn ffmpeg process")
    }
    pub fn error(&mut self) -> Option<String> {
        if let Some(child) = &mut self.child {
            if let Some(stderr) = &mut child.stderr {
                let mut str = String::new();
                let _ = stderr.read_to_string(&mut str);
                return Some(str);
            }
        }
        None
    }
}

impl Drop for FfmpegMpegtsStream {
    fn drop(&mut self) {
        if let Some(p) = &mut self.child {
            let _ = p.kill();
        }
    }
}

impl VideoStream for FfmpegMpegtsStream {
    fn read(&mut self, buf: &mut [u8]) -> std::io::Result<usize> {
        if self.child.is_none() {
            return Err(std::io::Error::new(
                std::io::ErrorKind::BrokenPipe,
                "Stream not initiialized.",
            ));
        }
        if let Some(stdout) = &mut self.stdout {
            let amount = stdout.read(buf)?;
            Ok(amount)
        } else {
            Err(std::io::Error::new(
                std::io::ErrorKind::BrokenPipe,
                "stdout not present",
            ))
        }
    }
    fn start(&mut self) {
        self.init();
    }
    fn stop(&mut self) {
        if let Some(mut child) = self.child.take() {
            let _ = child.kill();
        }
        self.stdout.take();
    }
    fn stream_state(&self) -> StreamState {
        match &self.child {
            Some(_) => StreamState::Running,
            None => StreamState::Idle,
        }
    }
}
