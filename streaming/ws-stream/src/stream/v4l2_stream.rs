#![allow(static_mut_refs)]

//! This module contains testing purpose ffmpeg stream that will work on desktops with no libcamera.

use std::net::UdpSocket;
/// Wrapper for a mp4 stream
use std::sync::OnceLock;

use anyhow::Result;
use crossbeam_queue::ArrayQueue;
use gstreamer::prelude::*;
use gstreamer::{FlowError, FlowSuccess, Pipeline};
use gstreamer_app::AppSink;

use super::StreamReadError;

type QueueDataType = Vec<u8>;

const QUEUE_CAPACITY: usize = 512;

/// Internal queue for gstreamer stream. Don't touch it, or nullptr dereferencing will happen somewhere.
static mut QUEUE: OnceLock<ArrayQueue<QueueDataType>> = OnceLock::new();
static mut UDP_SOCKET: OnceLock<std::net::UdpSocket> = OnceLock::new();

/// Callback for writing to the queue
fn callback(sink: &AppSink) -> Result<FlowSuccess, FlowError> {
    let sample = sink.pull_sample().map_err(|_| gstreamer::FlowError::Eos)?;
    let buffer = sample.buffer().ok_or(gstreamer::FlowError::Error)?;

    // Map the buffer and process the data
    let map = buffer
        .map_readable()
        .map_err(|_| gstreamer::FlowError::Error)?;
    // if the queue push failed (queue full), just ignore it and force.
    // There will be a jump in the video, but it will be more real time than waiting for it
    unsafe {
        let socket = UDP_SOCKET.get_mut().unwrap();
        // not to worry, it's non-blocking
        let _ = socket.send_to(&map, crate::config::MACHINE_VISION_PROCESS_ADDR);
        let queue = QUEUE.get_mut().unwrap();
        queue.force_push(map.to_vec());
    }
    Ok(FlowSuccess::Ok)
}
/// Initialize the gstreamer based queue and UDP socket for IPC. If it's not initialized, you'll get a null pointer somewhere (or Option unwrap);
fn init_statics() {
    unsafe {
        QUEUE.get_or_init(|| ArrayQueue::new(QUEUE_CAPACITY));
        UDP_SOCKET.get_or_init(|| {
            let  sock = UdpSocket::bind("127.0.0.1:0")
        .expect("Cannot bind a random ass UDP socket. Either you don't have a loopback interface, ports available or bad permissions. Either way, congratulations!");
         sock.set_nonblocking(true).expect("Cannot set UDP socket to nonblocking");
         sock
    });
    }
}

pub struct GstreamerV4L2Stream {
    pipeline: Pipeline,
}

impl GstreamerV4L2Stream {
    /// Create and initialize a gstreamer stream.
    pub fn init(config: &crate::config::Config) -> Result<Self> {
        init_statics();
        gstreamer::init()?;

        let pipeline = make_gst_pipeline::get_vl42_src_gstreamer_pipeline(config, callback)?;
        tracing::info!("Gstreamer pipeline set up, ready for streaming");

        Ok(Self { pipeline })
    }
}

impl crate::stream::VideoStream for GstreamerV4L2Stream {
    fn read(&mut self, buf: &mut [u8]) -> Result<usize, StreamReadError> {
        unsafe {
            let data = QUEUE
                .get()
                .expect("Queue not initialized, bad developer")
                .pop();
            match data {
                Some(vec) => {
                    let n = vec.len();
                    buf[0..n].copy_from_slice(&vec);
                    Ok(n)
                }
                None => Ok(0),
            }
        }
    }

    fn start(&mut self) {
        let _ = self.pipeline.set_state(gstreamer::State::Playing);
    }

    fn stop(&mut self) {
        let _ = self.pipeline.set_state(gstreamer::State::Paused);
    }

    fn stream_state(&self) -> crate::stream::StreamState {
        match self.pipeline.current_state() {
            gstreamer::State::VoidPending => crate::stream::StreamState::Idle,
            gstreamer::State::Null => crate::stream::StreamState::Errored,
            gstreamer::State::Ready => crate::stream::StreamState::Idle,
            gstreamer::State::Paused => crate::stream::StreamState::Idle,
            gstreamer::State::Playing => crate::stream::StreamState::Running,
        }
    }
}

pub(super) mod make_gst_pipeline {

    use gstreamer::{prelude::*, FlowError, FlowSuccess, Pipeline};
    use gstreamer_app::AppSink;

    #[derive(Default)]
    struct Elements {
        video: Vec<gstreamer::Element>,
        audio: Vec<gstreamer::Element>,
        mux: Option<gstreamer::Element>,
        sink: Option<AppSink>,
    }
    impl Elements {
        fn as_vec(&self) -> Vec<&gstreamer::Element> {
            let mut elements = Vec::new();
            elements.extend(self.video.iter());

            elements.extend(self.audio.as_slice());

            if let Some(mux) = &self.mux {
                elements.push(mux);
            }

            if let Some(sink) = &self.sink {
                elements.push(sink.upcast_ref());
            }

            elements
        }
    }

    /// Spawn a gstreamer pipeline with the corresponding callback with data.
    /// Requires libcamera process to work, as it's the easiest way to get hardware accelerated video.
    ///
    /// - `config` - app wide config
    /// - `callback` - a function to call when a new sample is pulled
    pub fn get_vl42_src_gstreamer_pipeline<F>(
        config: &crate::config::Config,
        callback: F,
    ) -> anyhow::Result<Pipeline>
    where
        F: 'static + Send + FnMut(&gstreamer_app::AppSink) -> Result<FlowSuccess, FlowError>,
    {
        let mut elements = Elements::default();
        let pipeline = gstreamer::Pipeline::default();

        let videosrc = gstreamer::ElementFactory::make("v4l2src").build()?; //used to use fd src with h264 encoded stream

        let videoconvert = gstreamer::ElementFactory::make("videoconvert").build()?;
        let x264enc = gstreamer::ElementFactory::make("x264enc").build()?;
        let h264parse = gstreamer::ElementFactory::make("h264parse").build()?;

        elements.video = vec![videosrc, videoconvert, x264enc, h264parse];

        if !config.no_audio {
            add_audio_elements(&mut elements)?;
        }

        let mpegtsmux = gstreamer::ElementFactory::make("mpegtsmux")
            .property("m2ts-mode", true)
            .build()?;
        elements.mux = Some(mpegtsmux);

        let appsink = gstreamer_app::AppSink::builder().build();
        appsink.set_callbacks(
            gstreamer_app::AppSinkCallbacks::builder()
                .new_sample(callback)
                .build(),
        );
        elements.sink = Some(appsink.upcast());

        pipeline.add_many(elements.as_vec())?;

        link_elements(&elements)?;

        Ok(pipeline)
    }

    /// Add audio elements to the gstreamer pipeline.
    /// May fail if no suitable audio backend is present.
    #[inline(always)]
    fn add_audio_elements(elements: &mut Elements) -> anyhow::Result<()> {
        match crate::stream::audio::get_audio_backend() {
            Some(backend) => {
                let audiosrc =
                    gstreamer::ElementFactory::make(backend.as_gstreamer_str()).build()?;
                let audioconvert = gstreamer::ElementFactory::make("audioconvert").build()?;
                let audioenc = gstreamer::ElementFactory::make("avenc_aac").build()?;
                elements.audio = vec![audiosrc, audioconvert, audioenc];

                Ok(())
            }
            None => {
                tracing::warn!("No audio backend present for the ffmpeg pipeline");
                Err(anyhow::Error::msg("cannot add audio"))
            }
        }
    }

    fn link_elements(elements: &Elements) -> anyhow::Result<()> {
        let mux = elements
            .mux
            .as_ref()
            .ok_or(anyhow::Error::msg("No muxer found for gstreamer pipeline"))?;

        // link video
        let mut base: Vec<&gstreamer::Element> = elements.video.as_slice().iter().collect();
        base.push(mux);
        gstreamer::Element::link_many(base)?;
        // link audio
        let mut base: Vec<&gstreamer::Element> = elements.audio.as_slice().iter().collect();
        base.push(mux);
        gstreamer::Element::link_many(base)?;

        mux.link(
            elements
                .sink
                .as_ref()
                .expect("no sink found, stream broken"),
        )?;

        Ok(())
    }
}
