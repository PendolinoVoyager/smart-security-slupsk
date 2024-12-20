use std::os::fd::IntoRawFd;
use std::process::Child;
use std::sync::OnceLock;

use anyhow::Result;
use crossbeam_queue::ArrayQueue;
use gstreamer::prelude::*;
use gstreamer::{FlowError, FlowSuccess, Pipeline};
use gstreamer_app::AppSink;

type QueueDataType = Vec<u8>;
const QUEUE_CAPACITY: usize = 512;
/// Internal queue for gstreamer stream. Don't touch it, or nullptr dereferencing will happen somewhere.
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
    libcamera_process: Child,
}

impl GStreamerLibcameraStream {
    /// Create and initialize a gstreamer stream.
    pub fn init(config: &crate::Config) -> Result<Self> {
        init_queue();
        gstreamer::init()?;

        let mut child = spawn_libcamera_process()?;

        let fd = match child.stdout.take() {
            Some(stdout) => Ok(stdout.into_raw_fd()),
            None => Err(std::io::Error::new(
                std::io::ErrorKind::BrokenPipe,
                "libcamera process stdout not present, cannot pipe",
            )),
        }?;
        // here its fine, reading data ok
        // let mut buf = [0_u8; 1024];
        // unsafe {
        //     loop {
        //         let data = libc::read(fd, buf.as_mut_ptr() as *mut libc::c_void, 1024);
        //         println!("{:?}", data);
        //     }
        // }
        tracing::info!("Created libcamera subprocess! stdout: {fd}");
        let pipeline = get_rpi_zero2w_pipeline(fd, config, callback)?;
        tracing::info!("Gstreamer pipeline set up, ready for streaming");

        Ok(Self {
            pipeline,
            libcamera_process: child,
        })
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
        let _ = self.libcamera_process.kill();
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

/// Spawn a gstreamer pipeline with the corresponding callback with data.
/// Requires libcamera process to work, as it's the easiest way to get hardware accelerated video.
///
/// - `fd` - file descriptor of the libcamera process's stdout (or any file that outputs raw h264 stream)
/// - `config` - app wide config
/// - `callback` - a function to call when a new sample is pulled
fn get_rpi_zero2w_pipeline<F>(
    fd: i32,
    config: &crate::Config,
    callback: F,
) -> anyhow::Result<Pipeline>
where
    F: 'static + Send + FnMut(&gstreamer_app::AppSink) -> Result<FlowSuccess, FlowError>,
{
    // Create the pipeline
    let pipeline = gstreamer::Pipeline::default();

    // video elements
    let videosrc = gstreamer::ElementFactory::make("fdsrc")
        // gstreamer expects fd to be i32
        .property("fd", fd)
        // timeout in micros
        .property("timeout", 5000_u64)
        .build()?;

    let parse = gstreamer::ElementFactory::make("h264parse").build()?;

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
        &parse,
        // &audiosrc,
        // &audioconvert,
        // &opusenc,
        &mpegtsmux,
        appsink.upcast_ref(),
    ])?;

    gstreamer::Element::link_many([&videosrc, &parse, &mpegtsmux])?;
    // gstreamer::Element::link_many([&audiosrc, &audioconvert, &opusenc, &mpegtsmux])?;
    mpegtsmux.link(&appsink)?;

    Ok(pipeline)
}

/// Spawn the libcamera process. Necessary to do so, as it basically guarantees hardware acceleration.
fn spawn_libcamera_process() -> std::io::Result<std::process::Child> {
    let mut command = std::process::Command::new("libcamera-vid");
    command.args([
        "--width",
        "640",
        "--height",
        "480",
        "--framerate",
        "24",
        "--inline",
        "-t",
        "0s",
    ]);
    command.arg("-o");
    command.arg("-");
    command.stdout(std::process::Stdio::piped());
    command.stdin(std::process::Stdio::null());
    command.stderr(std::process::Stdio::piped());
    command.spawn()
}
