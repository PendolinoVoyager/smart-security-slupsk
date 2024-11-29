const videoHandle = document.querySelector("#video");

let MIME_CODEC = 'video/webm; codecs="vp8, vorbis"';
const mediaSource = new MediaSource();
videoHandle.src = URL.createObjectURL(mediaSource); // Attach MediaSource to the video tag

let buf;
mediaSource.addEventListener("sourceopen", () => {
  console.log("MediaSource opened");
  buf = mediaSource.addSourceBuffer(MIME_CODEC);
  buf.mode = "segments"; // Set mode to "segments" for fragmented streams

  buf.addEventListener("updateend", () => {
    console.log("Buffer updated");
    if (videoHandle.paused) {
      videoHandle.play().catch(console.error); // Ensure video plays
    }
  });
});

const socket = new WebSocket("ws://192.168.8.124:8080");
socket.binaryType = "arraybuffer";

socket.addEventListener("message", (m) => {
  if (buf && !buf.updating && mediaSource.readyState === "open") {
    try {
      buf.appendBuffer(m.data);
      console.log(`Appended data: ${m.data.byteLength} bytes`);
    } catch (e) {
      console.error("Buffer append failed:", e);
    }
  }
});
