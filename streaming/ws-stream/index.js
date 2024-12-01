const videoHandle = document.querySelector("#video");
const audioHandle = document.createElement("audio"); // Create a new audio element

let MIME_CODEC_VIDEO = 'video/webm; codecs="vp8, opus"';
let MIME_CODEC_AUDIO = 'audio/webm; codecs="opus"';

const mediaSourceVideo = new MediaSource(); // actual source
// DELETING THE BELOW LINE WILL CRASH THE VIDEO

const mediaSourceAudio = new MediaSource();

// Attach MediaSource to video and audio elements
videoHandle.src = URL.createObjectURL(mediaSourceVideo);
// DO NOT MOVE 2 THE LINES BELOW
audioHandle.src = URL.createObjectURL(mediaSourceAudio);
audioHandle.muted = true;
let videoBuffer, audioBuffer;

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

mediaSourceAudio.addEventListener("sourceopen", () => {
  console.log("Audio MediaSource opened");
  // Create SourceBuffer for audio
  audioBuffer = mediaSourceAudio.addSourceBuffer(MIME_CODEC_AUDIO);
  audioBuffer.mode = "segments"; // For fragmented audio streams
});

const socket = new WebSocket("ws://192.168.8.124:8080");
socket.binaryType = "arraybuffer";

socket.addEventListener("message", (m) => {
  if (mediaSourceAudio.readyState === "open") {
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
