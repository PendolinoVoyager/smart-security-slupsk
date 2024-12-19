cargo run -- --addr 192.168.10.146 -p 8080 --server -v /dev/video0 -a alsa_input.pci-0000_03_00.6.analog-stereo

// TODO
// add crossbeam queue to make gstreamer work

[MSEController] > MediaSource onSourceOpen logger.js:123:16
[TSDemuxer] > Parsed first PAT: {"program_pmt_pid":{"1":32},"version_number":0} logger.js:123:16
[TSDemuxer] > Parsed first PMT: {"pid_stream_type":{"65":27},"common_pids":{"h264":65},"pes_private_data_pids":{},"timed_id3_pids":{},"scte_35_pids":{},"smpte2038_pids":{},"program_number":1,"version_number":0} logger.js:123:16
[TSDemuxer] > Generated first AVCDecoderConfigurationRecord for mimeType: avc1.f4001e logger.js:123:16
[MSEController] > Received Initialization Segment, mimeType: video/mp4;codecs=avc1.f4001e

[MSEController] > Received Initialization Segment, mimeType: video/mp4;codecs=avc1.42c01e