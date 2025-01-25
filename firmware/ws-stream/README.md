# Rust Firmware Kickstart Binary Crate

This crate provides a simple Rust-based binary to kickstart the firmware, handle video streaming to localhost UDP, and forward the stream to a remote server.

---

## Features

- **UDP Video Stream:** Pipes a video stream to `localhost` over UDP.
- **Server Forwarding:** Streams the video feed to a remote server.
- **Lightweight and Fast:** Built using Rust for optimal performance and low latency.

---

### Building the Crate

Clone the repository and build the crate with provided build.sh.
The build tool requires Docker to run.
You can deploy the firmware to local development device with deploy.sh.

---

## Additional Notes

- Ensure that the streaming server is ready to accept the video stream before starting the binary.

---
