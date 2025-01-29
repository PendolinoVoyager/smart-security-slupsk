#!/bin/bash

# This script should be run on startup.
# It will initialize the MPEG-TS stream and multicast it to relevant services on the device.
# This isn't optimal, but gets the job done and I/O cost isn't too big (for now!!!). 

LOCAL_MV_HOST="127.0.0.1"
LOCAL_MV_PORT="10000"

MPEGTS_HOST="127.0.0.1"
MPEGTS_PORT="5001"

# Check if libcamera-vid is installed
# It's only here so the test script can work with v4l2 for development purposes
if command -v libcamera-vid &> /dev/null; then
    echo "Using libcamera-vid..."
    
    libcamera-vid --width 1920 --height 1080 --framerate 18 --bitrate 1000000 --inline -t 0 -o - | \
    gst-launch-1.0 fdsrc fd=0 timeout=5000 ! tee name=t \
        t. ! queue ! h264parse ! udpsink host=$LOCAL_MV_HOST port=$LOCAL_MV_PORT sync=false \
        t. ! queue ! h264parse ! mpegtsmux m2ts-mode=true ! udpsink host=$MPEGTS_HOST port=$MPEGTS_PORT sync=false
else
    echo "libcamera-vid not found, using v4l2src as fallback..."
    
    gst-launch-1.0 v4l2src device=/dev/video0 ! videoconvert ! x264enc bitrate=1000 tune=zerolatency ! tee name=t \
        t. ! queue ! h264parse ! udpsink host=$LOCAL_MV_HOST port=$LOCAL_MV_PORT sync=false \
        t. ! queue ! h264parse ! mpegtsmux m2ts-mode=true ! udpsink host=$MPEGTS_HOST port=$MPEGTS_PORT sync=false
fi
