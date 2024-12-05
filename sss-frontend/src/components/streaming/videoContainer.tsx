import React, { useState } from "react";
import VideoControls from "./videoControls";
import VideoFeed from "./videoFeed";
import { Box } from "@mui/material";

const VideoContainer: React.FC = () => {
  const [currentStream, setCurrentStream] = useState<string>("");
  const [isPlaying, setIsPlaying] = useState<boolean>(false);

  const streams = [{ name: "DEBUG", url: "ws://192.168.10.21:8080" }];

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

  return (
    <Box margin={"1rem"}>
      <VideoControls
        streams={streams}
        onStreamSelect={handleStreamSelect}
        onStart={handleStart}
        onStop={handleStop}
        isPlaying={isPlaying}
      >
        {" "}
      </VideoControls>
      {currentStream && (
        <VideoFeed
          url={currentStream}
          mime="video/webm; codecs=vp8,opus"
          play={isPlaying}
        />
      )}
    </Box>
  );
};

export default VideoContainer;
