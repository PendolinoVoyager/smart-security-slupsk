"use client";
import React, { useState } from "react";
import VideoFeed from "./videoFeed";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { Play, Square } from "lucide-react";

interface VideoControllerProps {
  streamUrl: string;
}

/**
 * Client side component for controlling video feed.
 */
const VideoController: React.FC<VideoControllerProps> = ({ streamUrl }) => {
  const [isPlaying, setIsPlaying] = useState(false);

  const handleError = () => setIsPlaying(false);

  return (
    <div className="w-full max-w-lg mx-auto shadow-md overflow-hidden rounded-2xl">
      <CardContent className="p-0">
        <div className="aspect-video bg-black flex items-center justify-center">
          <VideoFeed url={streamUrl} play={isPlaying} onError={handleError} />
        </div>
      </CardContent>
      <CardFooter className="flex justify-center gap-3 py-4 bg-muted/50">
        <Button
          size="icon"
          variant={isPlaying ? "destructive" : "default"}
          onClick={() => setIsPlaying((cur) => !cur)}
          aria-label={isPlaying ? "Stop video" : "Start video"}
        >
          {isPlaying ? (
            <Square className="h-5 w-5" />
          ) : (
            <Play className="h-5 w-5" />
          )}
        </Button>
      </CardFooter>
    </div>
  );
};

export default VideoController;
