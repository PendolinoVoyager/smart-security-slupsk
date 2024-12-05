/* eslint-disable react-hooks/exhaustive-deps */
import React, { useEffect, useRef } from "react";
import WebSocketMSE from "./WebSocketMSE";
import { Paper } from "@mui/material";

interface VideoFeedProps {
  mime: string;
  url: string;
  play: boolean;
}
const VideoFeed: React.FC<VideoFeedProps> = ({ mime, url, play = false }) => {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const mediaSourceHandlerRef = useRef<WebSocketMSE | null>(null);
  useEffect(() => {
    const videoElement = videoRef.current;

    if (play) {
      if (videoElement) {
        mediaSourceHandlerRef.current = new WebSocketMSE(
          videoElement,
          url,
          mime
        );
      }
    }
    return () => {
      // Cleanup the media source handler
      mediaSourceHandlerRef.current?.destroy();
    };
  }, [play]);

  return (
    <Paper>
      <video
        ref={videoRef}
        poster="video-off.png"
        style={{
          maxHeight: "100%",
          maxWidth: "500px",
          borderRadius: "8px",
          boxShadow: "0 4px 12px rgba(0, 0, 0, 0.3)",
        }}
      />
    </Paper>
  );
};

export default VideoFeed;
