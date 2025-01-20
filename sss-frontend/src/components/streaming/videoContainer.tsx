import React, { useEffect, useState } from "react";
import VideoControls from "./videoControls";
import VideoFeed from "./videoFeed";
import { Box } from "@mui/material";
import { useFlash } from "../../store/flashStore";
import fetchStreams from "../../api/fetchStreams";
import { getToken } from "../../authUtils";

const VideoContainer: React.FC = () => {
  const [currentStream, setCurrentStream] = useState<string>("");
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const flash = useFlash();

  const [streams, setStreams] = useState<{ name: string; url: string }[]>([]);
  useEffect(() => {
    (async () => {
      const res = await fetchStreams();
      if (res instanceof Error) {
        flash.addFlash(
          "error",
          "Connection error, failed to fetch streams:\n" + res
        );
        return;
      }
      const r = res.map((v) => {
        return {
          url:
            "ws://" +
            v.server_addr +
            `/stream?device_id=${v.id}&token=${getToken()}`,
          name: v.device_name,
        };
      });
      setStreams(r);
    })();
  }, []);
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
    flash.addFlash("error", "Unexpected end of stream!");
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
