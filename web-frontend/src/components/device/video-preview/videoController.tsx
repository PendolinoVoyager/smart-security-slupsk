"use client";
import React, { useState } from "react";
import VideoFeed from "./videoFeed";
import { Button } from "@/components/ui/button";

interface VideoControllerProps {
  streamUrl: string;
}
/**
 * Client side component for controlling video feed.
 */
const VideoController: React.FC<VideoControllerProps> = function ({
  streamUrl,
}) {
  const [isPlaying, setIsPlaying] = useState<boolean>(false);

  const handleStart = () => {
    setIsPlaying(true);
  };

  const handleStop = () => {
    setIsPlaying(false);
  };

  const handleError = () => {
    setIsPlaying(false);
  };
  return (
    <div>
      <Button
        onClick={() => {
          setIsPlaying((cur) => !cur);
        }}
      >
        Toggle play
      </Button>
      <VideoFeed url={streamUrl} play={isPlaying} onError={handleError} />
    </div>
  );
};
export default VideoController;
