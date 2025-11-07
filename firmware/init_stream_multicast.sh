#!/bin/bash

# This script starts video production and a simple http server for preview
SERVER_DIR="/var/localstream/html"
HLS_OUTPUT_DIR="$SERVER_DIR/stream"
PLAYLIST_NAME="index.m3u8"

MPEGTS_HOST="127.0.0.1"
MPEGTS_PORT="10001"

if [ ! -f /var/localstream/html ]; then
rm -rf "$HLS_OUTPUT_DIR"/*
else
echo "Cannot start stream. Make sure $SERVER_DIR exists and user $USER has permissions to write in it."
exit 1
fi
# Start a simple HTTP server
python3 hls_server.py &

if command -v libcamera-vid &> /dev/null; then
    echo "Using libcamera-vid..."
    pkill -9 libcamera 2>/dev/null
    libcamera-vid --width 1920 --height 1080 --framerate 18 --bitrate 1000000 --inline -n -t 0 -o - | \
    gst-launch-1.0 fdsrc fd=0 ! video/x-h264,stream-format=byte-stream ! h264parse ! tee name=t \
        t. ! queue ! mpegtsmux name=mux m2ts-mode=true ! hlssink \
            playlist-location="$HLS_OUTPUT_DIR/$PLAYLIST_NAME" \
            location="$HLS_OUTPUT_DIR/segment_%05d.ts" \
            target-duration=3 max-files=5 \
        t. ! queue ! mux. \
        t. ! queue ! udpsink host=$MPEGTS_HOST port=$MPEGTS_PORT sync=false ts-offset=-1
else
    echo "libcamera-vid not found, using v4l2src as fallback..."
    pkill -9 gst-launch 2>/dev/null
    gst-launch-1.0 v4l2src device=/dev/video0 ! videoconvert ! x264enc tune=zerolatency bitrate=1000 speed-preset=ultrafast ! \
        h264parse ! tee name=t \
        t. ! queue ! mpegtsmux name=mux m2ts-mode=true ! hlssink \
            playlist-location="$HLS_OUTPUT_DIR/$PLAYLIST_NAME" \
            location="$HLS_OUTPUT_DIR/segment_%05d.ts" \
            target-duration=3 max-files=5 \
        t. ! queue ! mux. \
        t. ! queue ! udpsink host=$MPEGTS_HOST port=$MPEGTS_PORT sync=false
fi
