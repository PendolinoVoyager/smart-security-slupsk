const videoHandle = document.querySelector("#video");
const audioHandle = document.createElement("audio");

const MIME_CODEC = 'video/webm; codecs="vp8, opus"';

const mediaSource = new MediaSource();
const url = URL.createObjectURL(mediaSource);
// audioHandle.src = url;
videoHandle.src = url;

let videoBuffer;

mediaSource.addEventListener("sourceopen", () => {
  console.log("MediaSource opened");
  // Create SourceBuffer for video (only video data)
  videoBuffer = mediaSource.addSourceBuffer('video/webm; codecs="vp8, opus"');
  videoBuffer.mode = "sequence";
  videoBuffer.timestampOffset = 0;
  videoBuffer.appendWindowEnd = Infinity;
  // videoBuffer.addEventListener("updateend", () => {

  // });
  videoBuffer.addEventListener("error", (e) => {
    console.error("Video buffer error:", e);
    for (let i = 0; i < videoBuffer.buffered.length; i++) {
      console.log(
        "Buffered range:",
        videoBuffer.buffered.start(i),
        videoBuffer.buffered.end(i)
      );
    }
  });
});

const socket = new WebSocket("ws://192.168.8.124:8080");
socket.binaryType = "arraybuffer";

socket.addEventListener("message", (m) => {
  if (mediaSource.readyState === "open") {
    try {
      if (!videoBuffer.updating) {
        videoBuffer.appendBuffer(m.data);
        console.log(`Appended video data: ${m.data.byteLength} bytes`);
      }
    } catch (e) {
      console.error("Buffer append failed:", e);
    }
  }
});
