const videoHandle = document.querySelector("#video");

let MIME_CODEC_VIDEO = 'video/webm; codecs="vp8,opus"';

const mediaSourceVideo = new MediaSource(); // actual source

// Attach MediaSource to video and audio elements
videoHandle.src = URL.createObjectURL(mediaSourceVideo);

let videoBuffer;

mediaSourceVideo.addEventListener("sourceopen", () => {
  console.log("Video MediaSource opened");
  // Create SourceBuffer for video (which will be ignored)
  videoBuffer = mediaSourceVideo.addSourceBuffer(MIME_CODEC_VIDEO);
  videoBuffer.mode = "segments";
  videoBuffer.addEventListener("updateend", () => {
    if (videoHandle.paused) {
      videoHandle.play().catch(console.error); // Ensure video plays (for video, but not necessary if ignored)
      videoHandle.removeAttribute("error");
    }
  });
  videoBuffer.addEventListener("error", (e) =>
    console.error("Video buffer error:", e)
  );
});

const socket = new WebSocket("ws://192.168.10.21:8080");
socket.binaryType = "arraybuffer";

socket.addEventListener("message", (m) => {
  if (mediaSourceVideo.readyState === "open") {
    try {
      // Assume data type can distinguish between video and audio
      if (!videoBuffer.updating) {
        videoBuffer.appendBuffer(m.data);
      }
    } catch (e) {
      console.error("Buffer append failed:", e);
    }
  }
});
