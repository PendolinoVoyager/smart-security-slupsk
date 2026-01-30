#!/bin/bash

# This script starts video production and a simple HTTP server for preview
SERVER_DIR="/var/localstream/http"
HLS_OUTPUT_DIR="$SERVER_DIR/stream"
PLAYLIST_NAME="index.m3u8"

MPEGTS_HOST="127.0.0.1"
MPEGTS_PORT="10001"

# Ensure HLS output directory exists
#if [ ! -w "$HLS_OUTPUT_DIR" ]; then
#    echo "$HLS_OUTPUT_DIR is not writable. Please check permissions."
#    exit 1
#fi

#rm -rf $HLS_OUTPUT_DIR/*


# Start a simple HTTP server in background
# python3 /usr/sbin/hls_server.py &

# Use rpicam if available, else fallback to v4l2src
if command -v rpicam-vid &> /dev/null; then
    echo "Using rpicam for Pi camera..."
    pkill -9 rpicam-vid 2>/dev/null
    
    # This uses 100CPU just to stream hls because of encoding weirdness... Skip for now
    :'
    rpicam-vid --framerate 15 -g 300 -n --inline\
	    --encoder-libs "tune=zerolatency;key-int-max=30;speed-preset=ultrafast;bitrate=500" \
	     -t 0 -o - | \
    gst-launch-1.0 fdsrc fd=0 is-live=true ! \
	video/x-h264, width=640, height=480, framerate=15/1, stream-format=byte-stream ! \
	h264parse config-interval=1 ! \
	tee name=t \a
        t. ! queue max-size-buffers=3 leaky=downstream ! \
        	avdec_h264 ! x264enc ! mpegtsmux ! \
		hlssink \
               playlist-location="$hls_output_dir/$playlist_name" \
                location="$hls_output_dir/segment_%05d.ts" \
                target-duration=2 \
                max-files=5 \
                playlist-length=3 \
	t. ! queue max-size-buffers=3 max-size-time=0 max-size-bytes=0 leaky=downstream ! \
            mpegtsmux m2ts-mode=true ! \
            udpsink host=$mpegts_host port=$mpegts_port sync=false async=false \
            ts-offset=0
	'
	
    rpicam-vid --framerate 15 -g 60 -n --inline\
	    --encoder-libs "tune=zerolatency;speed-preset=ultrafast;bitrate=500" \
	     -t 0 -o - | \
    gst-launch-1.0 fdsrc fd=0 is-live=true ! \
	video/x-h264, width=640, height=480, framerate=15/1, stream-format=byte-stream ! \
	h264parse config-interval=1 ! \
            mpegtsmux m2ts-mode=true ! \
            udpsink host=$MPEGTS_HOST port=$MPEGTS_PORT \
            ts-offset=0
else
    echo "rpicam not found, using v4l2src fallback..."
    pkill -9 gst-launch 2>/dev/null
 
    gst-launch-1.0 -v \
    v4l2src device=/dev/video0 ! \
    video/x-raw,format=YUY2,width=1280,height=720,framerate=10/1 ! \
    videoconvert ! \
    video/x-raw,format=I420 ! \
    x264enc tune=zerolatency bitrate=1500 speed-preset=ultrafast key-int-max=20 ! \
    h264parse config-interval=1 ! \
    video/x-h264,stream-format=byte-stream,alignment=au ! \
    queue ! \
    mpegtsmux m2ts-mode=true ! \
    udpsink host=$MPEGTS_HOST port=$MPEGTS_PORT sync=false async=false

fi
