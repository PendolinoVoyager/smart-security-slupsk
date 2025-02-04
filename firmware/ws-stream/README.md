# Streaming Client 
## Overview

The streaming client is a lightweight component responsible for connecting to a streaming server and forwarding media streams via whatever communication protocol is used at the moment. It acts as a bridge between a local video pipeline (GStreamer as of Jan. 2025) and the central streaming server.
Features

- No reconnections, any failure in pipeline will early return.

- stream ingestion from local pipeline (works on raw bytes)

- Diagnostic logging with adjustable verbosity

- Binary data streaming with efficiency and simplicity in mind

## Prerequisites

    Rust 1.70+ (with Cargo)

    GStreamer 1.20+ with development libraries

    Tokio runtime

    Access to device credentials (token) in the file or with manual override


## Usage

Firstly:    **Start video pipeline with a shell script provided in firmware module**.

Then, run via command-line arguments, either manually or as an automated service:
```bash
./ws-stream --addr ws://server:port [--silent]
```

Error Handling

The client simply shuts down when anything goes wrong. It isn't ideally by any means \
but the binary can be simplified that way. The logic responsible for reconnection \
can be outsourced to more convinient solutions..

## Build process
Build the binary via build.sh script provided. This will cross-compile to the product's architecture (ARM64 as for Jan 2025). \
It requires docker to run. \
If by some reason the cross-compilation fails, check Cross.toml. \
You may add an additional step to install any dependencies needed for the build.