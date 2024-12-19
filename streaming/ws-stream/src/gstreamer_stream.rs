use std::sync::OnceLock;

use anyhow::Result;
use crossbeam_queue::ArrayQueue;
use gstreamer::prelude::*;
use gstreamer::{FlowError, FlowSuccess, Pipeline};
use gstreamer_app::AppSink;

const RPI_ZERO2W_STD_LIBCAMERA_FORMAT: &str = "video/x-raw,width=1296,height=972,framerate=30/1";

type QueueDataType = Vec<u8>;
const QUEUE_CAPACITY: usize = 256;

static mut QUEUE: OnceLock<ArrayQueue<QueueDataType>> = OnceLock::new();

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
    #[allow(static_mut_refs)]
    unsafe {
        let queue = QUEUE.get_mut().unwrap();
        queue.force_push(map.to_vec());
    }
    Ok(FlowSuccess::Ok)
}
/// Initialize the gstreamer based queue. If it's not initialized, you'll get a null pointer somewhere (or Option unwrap);
fn init_queue() {
    unsafe {
        #[allow(static_mut_refs)]
        QUEUE.get_or_init(|| ArrayQueue::new(QUEUE_CAPACITY));
    }
}

pub struct GStreamerLibcameraStream {
    pipeline: Pipeline,
}

impl GStreamerLibcameraStream {
    /// Create and initialize a gstreamer stream.
    pub fn init(config: &crate::Config) -> Result<Self> {
        init_queue();
        gstreamer::init()?;

        let pipeline = get_rpi_zero2w_pipeline(config, callback)?;

        Ok(Self { pipeline })
    }
}

impl crate::stream::VideoStream for GStreamerLibcameraStream {
    fn read(&mut self, buf: &mut [u8]) -> std::io::Result<usize> {
        unsafe {
            #[allow(static_mut_refs)]
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

fn get_rpi_zero2w_pipeline<F>(config: &crate::Config, callback: F) -> anyhow::Result<Pipeline>
where
    F: 'static + Send + FnMut(&gstreamer_app::AppSink) -> Result<FlowSuccess, FlowError>,
{
    // Create the pipeline
    let pipeline = gstreamer::Pipeline::default();

    // video elements
    let videosrc = gstreamer::ElementFactory::make("libcamerasrc").build()?;
    let videoconvert = gstreamer::ElementFactory::make("videoconvert").build()?;
    // let video_f = gstreamer::ElementFactory::make(RPI_ZERO2W_STD_LIBCAMERA_FORMAT).build()?;
    let x264enc = gstreamer::ElementFactory::make("x264enc")
        .property_from_str("tune", "zerolatency")
        .build()?;

    // audio elements
    // let audiosrc = gstreamer::ElementFactory::make("pulsesrc").build()?;
    // let audioconvert = gstreamer::ElementFactory::make("audioconvert").build()?;
    // let opusenc = gstreamer::ElementFactory::make("avenc_aac").build()?;

    let mpegtsmux = gstreamer::ElementFactory::make("mpegtsmux").build()?;
    let _appsink = gstreamer::ElementFactory::make("appsink")
        .property("emit-signals", true)
        .property("sync", false)
        .build()?;

    let appsink = gstreamer_app::AppSink::builder().build();
    appsink.set_callbacks(
        gstreamer_app::AppSinkCallbacks::builder()
            .new_sample(callback)
            .build(),
    );

    pipeline.add_many([
        &videosrc,
        // &video_f,
        &videoconvert,
        &x264enc,
        // &audiosrc,
        // &audioconvert,
        // &opusenc,
        &mpegtsmux,
        appsink.upcast_ref(),
    ])?;

    gstreamer::Element::link_many([&videosrc, &videoconvert, &x264enc, &mpegtsmux])?;
    // gstreamer::Element::link_many([&audiosrc, &audioconvert, &opusenc, &mpegtsmux])?;
    mpegtsmux.link(&appsink)?;

    Ok(pipeline)
}
