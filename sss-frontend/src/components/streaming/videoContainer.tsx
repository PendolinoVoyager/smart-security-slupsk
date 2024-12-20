import React, { useState } from "react";
import VideoControls from "./videoControls";
import VideoFeed from "./videoFeed";
import { Box } from "@mui/material";

const VideoContainer: React.FC = () => {
  const [currentStream, setCurrentStream] = useState<string>("");
  const [isPlaying, setIsPlaying] = useState<boolean>(false);

  const streams = [{ name: "DEBUG", url: "ws://192.168.8.156:8080/stream" }];

  const handleStreamSelect = (stream: string) => {
    setCurrentStream(stream);
    setIsPlaying(false); // Reset play state when switching streams
  };

  const handleStart = () => {
    if (currentStream) {
      setIsPlaying(true);
    }
  };

  const handleStop = () => {
    setIsPlaying(false);
  };

  const handleError = () => {
    setIsPlaying(false);
  };
  return (
    <Box margin={"1rem"}>
      <VideoControls
        streams={streams}
        onStreamSelect={handleStreamSelect}
        onStart={handleStart}
        onStop={handleStop}
        isPlaying={isPlaying}
      ></VideoControls>
      {currentStream && (
        <VideoFeed url={currentStream} play={isPlaying} onError={handleError} />
      )}
    </Box>
  );
};

export default VideoContainer;
