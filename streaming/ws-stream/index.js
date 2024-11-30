const videoHandle = document.querySelector("#video");
const audioHandle = document.createElement("audio"); // Create a new audio element

let MIME_CODEC = 'video/webm; codecs="vp8, opus"';

const mediaSource = new MediaSource();

const url = URL.createObjectURL(mediaSource);
audioHandle.src = url;

videoHandle.src = url;

// Append audio element to DOM for testing (optional)
document.body.appendChild(audioHandle);

let videoBuffer, audioBuffer;

mediaSource.addEventListener("sourceopen", () => {
  console.log("MediaSource opened");
  // Create SourceBuffer for video (only video data)
  videoBuffer = mediaSource.addSourceBuffer('video/webm; codecs="vp8, opus"');
  videoBuffer.mode = "segments"; // For fragmented video streams

  videoBuffer.addEventListener("updateend", () => {
    if (videoHandle.paused && !videoBuffer.updating) {
      videoHandle.play().catch(console.error); // Ensure video plays
    }
  });
  videoBuffer.addEventListener("error", (e) =>
    console.error("Video buffer error:", e)
  );

  audioBuffer = mediaSource.addSourceBuffer('audio/webm; codecs="opus"');
  audioBuffer.mode = "segments"; // For fragmented audio streams

  audioBuffer.addEventListener("updateend", () => {
    if (audioHandle.paused && !audioBuffer.updating) {
      audioHandle.play().catch(console.error); // Ensure audio plays
    }
  });
  audioBuffer.addEventListener("error", (e) =>
    console.error("Audio buffer error:", e)
  );
});

const socket = new WebSocket("ws://192.168.8.124:8080");
socket.binaryType = "arraybuffer";

socket.addEventListener("message", (m) => {
  if (mediaSource.readyState === "open") {
    try {
      // Check if audio buffer is not updating, then append audio data
      if (!audioBuffer.updating) {
        audioBuffer.appendBuffer(m.data);
        console.log(`Appended audio data: ${m.data.byteLength} bytes`);
      }

      if (!videoBuffer.updating) {
        // You may need to handle video data separately here (e.g., by checking m.data type/structure)
        videoBuffer.appendBuffer(m.data);
        console.log(`Appended video data: ${m.data.byteLength} bytes`);
      }
    } catch (e) {
      console.error("Buffer append failed:", e);
    }
  }
});
