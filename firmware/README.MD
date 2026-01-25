This is a directory for all of the code running on the device.

# Streaming Guide

1. Build (cross compile) the streaming app with ./scripts/build-ws-stream.sh if you need.
2. Run ./scripts/deploy-video.sh to copy files needed for streaming to RaspberryPi via SSH.
3. SSH into your RaspberryPi.
4. To start streaming, first run `init_stream_multicast.sh`. It's located in /usr/local/bin, so you can run it directly without providing path.
5. Ignore warnings if there are any.
6. After runnning step 4. you can run `ws-stream --addr ws://<streaming-server-ip>:9080 --token <token OR 100 for debug>`. This will stream to streaming server until it errors out or something.
Ideally, it should be run with a service with auto restart in case of connection failure, but it's what it is for now.
7. You should see a stream on the frontend. It should have audio if a microphone is connected to RaspberryPi, if not, tell me :P.

# Firmware Module Configuration and Token File Guide

## Configuration File

The configuration files are located at:

```
/etc/sss_firmware/*
```

## Email file

### Purpose:

_/etc/sss_firmware/email.txt_

This file contains current owner's email. The absence of any contents in the file means that the device isn't configured.

### Structure:

Is empty or contains a single line with user's email.

## Token File

The token file is located at:

```
/etc/sss_firmware/token.txt
```

### Purpose:

The token file is used for secure authentication with external services or APIs.

### Structure:

The file should contain:

1. **API Token**: A single line with the API token or secret key.
