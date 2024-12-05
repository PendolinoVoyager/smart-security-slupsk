export default class WebSocketMSE {
  private videoElement: HTMLVideoElement;
  private mediaSource: MediaSource;
  private videoBuffer: SourceBuffer | null = null;
  private socket: WebSocket | null = null;
  private MIME_CODEC_VIDEO = 'video/webm; codecs="vp8,opus"';

  constructor(videoElement: HTMLVideoElement, socketUrl: string, mime: string) {
    this.videoElement = videoElement;
    this.mediaSource = new MediaSource();
    this.MIME_CODEC_VIDEO = mime;
    // Set video element source to MediaSource
    this.videoElement.src = URL.createObjectURL(this.mediaSource);

    // Event listeners for MediaSource
    this.mediaSource.addEventListener("sourceopen", this.handleSourceOpen);

    // Set up WebSocket
    this.setupWebSocket(socketUrl);
  }

  private handleSourceOpen = () => {
    console.log("Video MediaSource opened");

    try {
      this.videoBuffer = this.mediaSource.addSourceBuffer(
        this.MIME_CODEC_VIDEO
      );
      this.videoBuffer.mode = "segments";

      this.videoBuffer.addEventListener("updateend", () => {
        if (this.videoElement.paused) {
          this.videoElement.play().catch(console.error);
        }
      });

      this.videoBuffer.addEventListener("error", (e) =>
        console.error("Video buffer error:", e)
      );
    } catch (error) {
      console.error("Error adding source buffer:", error);
    }
  };

  private setupWebSocket(socketUrl: string) {
    this.socket = new WebSocket(socketUrl);
    this.socket.binaryType = "arraybuffer";

    this.socket.addEventListener("message", this.handleSocketMessage);
    this.socket.addEventListener("error", (error) =>
      console.error("WebSocket error:", error)
    );
  }

  private handleSocketMessage = (message: MessageEvent<ArrayBuffer>) => {
    if (
      this.mediaSource.readyState === "open" &&
      this.videoBuffer &&
      !this.videoBuffer.updating
    ) {
      try {
        this.videoBuffer.appendBuffer(message.data);
      } catch (error) {
        console.error("Buffer append failed:", error);
      }
    }
  };
  public reset() {
    if (this.videoBuffer && this.mediaSource.readyState === "open") {
      this.mediaSource.endOfStream();
      this.videoElement.src = "";
    }
    this.socket?.close();
  }
  public destroy() {
    this.reset();
  }
}
