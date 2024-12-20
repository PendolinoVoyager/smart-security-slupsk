cargo run -- --addr 192.168.10.146 -p 8080 --server -v /dev/video0 -a alsa_input.pci-0000_03_00.6.analog-stereo


libcamera-vid --width 640 --height 480 --framerate 24 -t 0s  -o - | gst-launch-1.0 fdsrc ! h264parse ! mpegtsmux ! filesink location=./test.ts

TODO: buffering doesn't work when queue overflows