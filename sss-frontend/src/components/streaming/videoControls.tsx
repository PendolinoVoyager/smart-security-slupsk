import React, { PropsWithChildren, useState } from "react";
import {
  Box,
  Button,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  SelectChangeEvent,
} from "@mui/material";

interface VideoControlsProps extends PropsWithChildren {
  streams: { name: string; url: string }[]; // List of stream URLs
  onStreamSelect: (stream: string) => void; // Callback when stream is selected
  onStart: () => void; // Callback to start video
  onStop: () => void; // Callback to stop video
  isPlaying: boolean;
}

const VideoControls: React.FC<VideoControlsProps> = ({
  streams,
  onStreamSelect,
  onStart,
  onStop,
  isPlaying,
  children,
}) => {
  const [selectedStream, setSelectedStream] = useState<string>("");

  const handleStreamChange = (event: SelectChangeEvent) => {
    const stream = event.target.value as string;
    setSelectedStream(stream);
    onStreamSelect(stream); // Notify parent about selected stream
  };

  return (
    <Box display="flex" flexDirection="column" alignItems="center" gap={2}>
      {children}

      <FormControl fullWidth>
        <InputLabel id="stream-select-label">Select Stream</InputLabel>
        <Select
          labelId="stream-select-label"
          value={selectedStream}
          label="Select Stream"
          onChange={handleStreamChange}
          size="small"
          fullWidth={false}
        >
          {streams.map((stream, index) => (
            <MenuItem key={index} value={stream.url}>
              {stream.name}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      <Box display="flex" gap={2}>
        <Button
          variant="contained"
          color="primary"
          onClick={onStart}
          disabled={!selectedStream || isPlaying}
        >
          Start
        </Button>
        <Button
          variant="contained"
          color="secondary"
          onClick={onStop}
          disabled={!isPlaying}
        >
          Stop
        </Button>
      </Box>
    </Box>
  );
};

export default VideoControls;
