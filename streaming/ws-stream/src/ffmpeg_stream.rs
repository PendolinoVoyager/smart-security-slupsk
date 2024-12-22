#![allow(unused)]

//! This module is responsible for providing an API for a MP4 stream.
//! It might use ffmpeg but that may change in the future.

use std::io::Read;
use std::process::{Child, ChildStdout, Stdio};

use crate::audio::get_audio_backend;
use crate::stream::{StreamReadError, StreamState, VideoStream};
use crate::Config;

/// Wrapper for a mp4 stream
pub struct FfmpegMpegtsStream {
    /// Video device currently used. Defaults to "/dev/video0"
    pub video_dev: String,
    /// Audio device currently used
    pub audio: bool,
    child: Option<Child>,
    stdout: Option<ChildStdout>,
}

impl FfmpegMpegtsStream {
    pub fn new(config: &Config) -> Self {
        Self {
            child: None,
            video_dev: "/dev/video0".into(),
            audio: !config.no_audio,
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

        if self.audio {
            match get_audio_backend() {
                Some(backend) => {
                    command
                        .arg("-f")
                        .arg(backend.as_ffmpeg_str())
                        .arg("-i")
                        .arg("default")
                        .arg("-c:a")
                        .arg("libopus")
                        .arg("-ac")
                        .arg("1");
                }
                None => {
                    tracing::warn!("No audio backend present for the ffmpeg pipeline")
                }
            }
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
    fn read(&mut self, buf: &mut [u8]) -> Result<usize, StreamReadError> {
        if self.child.is_none() {
            return Err(StreamReadError::Paused);
        }
        if let Some(stdout) = &mut self.stdout {
            let amount = stdout.read(buf)?;
            Ok(amount)
        } else {
            Err(StreamReadError::PipelineBroken)
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
