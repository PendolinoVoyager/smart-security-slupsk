/* eslint-disable react-hooks/exhaustive-deps */
"use client";
import React, { useEffect, useRef } from "react";
import mpegts from "mpegts.js";

interface VideoFeedProps {
  /** WebSocket url to connect to.
   */
  url: string;
  /** Control playing/not playing state.
   */
  play: boolean;
  /** OnError / stream end callback
   */
  onError?: (e: Error | null) => void;
}
/**
 * Component for displaying raw MPEG video feed. It's just a HTML video element that plays from WebSocket URL source.
 */
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
          player.current = mpegts.createPlayer(
            {
              type: "m2ts",
              isLive: true,
              url,
            },
            {
              // This is a lie! It doesn't work, JS doesn't allow any headers
              // headers: { "Authorization": "Userino :)" },
              isLive: true,
              liveBufferLatencyChasing: true,
              liveBufferLatencyMaxLatency: 2.0,
              liveBufferLatencyMinRemain: 1.0,
            }
          );
          player.current.attachMediaElement(videoElement);
          player.current.load();
          player.current.play();

          player.current.on(mpegts.Events.LOADING_COMPLETE, () => {
            onError(null);
          });
          player.current.on("error", (e) => {
            onError(e);
          });
        } catch (e: unknown) {
          if (e instanceof Error) {
            onError(e);
          }
        }
      }
    }

    return () => {
      player.current?.destroy();
      player.current = null;
    };
  }, [play]);

  return (
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
  );
};

export default VideoFeed;
