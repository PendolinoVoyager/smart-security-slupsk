/// Test if a valid file is outputted with ffprobe, so there's no need to test it on browser.
use std::io::Write;

use anyhow::Error;
use gstreamer as gst;
use gstreamer::prelude::*;
use gstreamer_app as gst_app;

const TEST_FILE: &str = "out.ts";

fn create_pipeline() -> anyhow::Result<gst::Pipeline> {
    gst::init()?;

    // Create the pipeline
    let pipeline = gst::Pipeline::default();

    // video elements
    let v4l2src = gstreamer::ElementFactory::make("v4l2src").build().unwrap();
    let videoconvert = gstreamer::ElementFactory::make("videoconvert")
        .build()
        .unwrap();
    let x264enc = gstreamer::ElementFactory::make("x264enc").build().unwrap();
    let mpegtsmux = gstreamer::ElementFactory::make("mpegtsmux")
        .build()
        .unwrap();

    // audio elements
    let audiosrc = gstreamer::ElementFactory::make("pulsesrc")
        .build()
        .expect("Failed to create audiosrc element");
    let audioconvert = gstreamer::ElementFactory::make("audioconvert")
        .build()
        .expect("Failed to create audioconvert element");
    let opusenc = gstreamer::ElementFactory::make("opusenc")
        .build()
        .expect("Failed to create opusenc element");

    let _appsink = gstreamer::ElementFactory::make("appsink")
        .property("emit-signals", true)
        .property("sync", false)
        .build()
        .unwrap();

    let appsink = gst_app::AppSink::builder().build();

    // Set the appsink properties
    let _ = std::fs::remove_file(TEST_FILE);
    let mut file = std::fs::OpenOptions::new()
        .append(true)
        .create_new(true)
        .open(TEST_FILE)
        .unwrap();

    appsink.set_callbacks(
        gst_app::AppSinkCallbacks::builder()
            .new_sample(move |appsink| {
                let sample = appsink.pull_sample().map_err(|_| gst::FlowError::Eos)?;

                let buffer = sample.buffer().ok_or(gst::FlowError::Error)?;

                // Map the buffer and process the data
                let map = buffer.map_readable().map_err(|_| gst::FlowError::Error)?;

                let _ = file.write(map.as_slice());

                println!("Got {} bytes of video data", map.len());

                Ok(gst::FlowSuccess::Ok)
            })
            .build(),
    );

    // Add elements to the pipeline
    pipeline.add_many([
        &v4l2src,
        &videoconvert,
        &x264enc,
        &audiosrc,
        &audioconvert,
        &opusenc,
        &mpegtsmux,
        appsink.upcast_ref(),
    ])?;

    // Link elements
    gst::Element::link_many([&v4l2src, &videoconvert, &x264enc, &mpegtsmux])?;
    gst::Element::link_many([&audiosrc, &audioconvert, &opusenc, &mpegtsmux])?;
    mpegtsmux.link(&appsink)?;

    Ok(pipeline)
}

fn main_loop(pipeline: gst::Pipeline) -> anyhow::Result<()> {
    pipeline.set_state(gst::State::Playing)?;

    let bus = pipeline
        .bus()
        .expect("Pipeline without bus. Shouldn't happen!");

    for msg in bus.iter_timed(gst::ClockTime::NONE) {
        use gst::MessageView;

        match msg.view() {
            MessageView::Eos(..) => break,
            MessageView::Error(err) => {
                pipeline.set_state(gst::State::Null)?;
                println!("{:?}", err);
                return Err(Error::msg("Pipeline error"));
            }
            _ => (),
        }
    }

    pipeline.set_state(gst::State::Null)?;

    Ok(())
}

#[test]
fn main() {
    let pipeline = create_pipeline().unwrap();
    println!("{:?}", main_loop(pipeline));
    let _ = std::fs::remove_file(TEST_FILE);
}
