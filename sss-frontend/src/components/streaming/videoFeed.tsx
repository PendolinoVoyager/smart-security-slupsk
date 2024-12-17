/* eslint-disable react-hooks/exhaustive-deps */
import React, { useEffect, useRef } from "react";
import { Paper } from "@mui/material";
import mpegts from "mpegts.js";

interface VideoFeedProps {
  url: string;
  play: boolean;
  onError?: (e: Error) => void;
}
const VideoFeed: React.FC<VideoFeedProps> = ({
  url,
  play = false,
  onError = () => {},
}) => {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const player = useRef<mpegts.Player | null>(null);
  useEffect(() => {
    const videoElement = videoRef.current;

    if (play && videoElement) {
      if (mpegts.getFeatureList().mseLivePlayback) {
        try {
          player.current = mpegts.createPlayer({
            type: "mse",
            isLive: true,
            url,
          });
          player.current.attachMediaElement(videoElement);
          player.current.load();
          player.current.play();
          player.current.on("error", (e) => {
            onError(e);
          });
        } catch (e: unknown) {
          if (e instanceof Error) {
            onError(e);
          }
          alert("Stream failed, device offline!");
        }
      }
    }

    return () => {
      player.current?.destroy();
      player.current = null;
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
        id="videoElement"
        preload="none"
      />
    </Paper>
  );
};

export default VideoFeed;
