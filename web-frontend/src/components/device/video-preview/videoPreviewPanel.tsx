"use server";
import { FC } from "react";
import { getStreamAvailability } from "@/api/stream";
import { getAuthData } from "@/lib/auth/server";
import VideoController from "./videoController";

interface VideoPreviewPanelProps {
  deviceId: number;
}
const VideoPreviewPanel: FC<VideoPreviewPanelProps> = async function ({
  deviceId,
}) {
  const authData = await getAuthData();
  if (!authData) {
    return <div>Error: Unathenticated</div>;
  }

  const stream = await getStreamAvailability(authData.token, deviceId);

  if (stream instanceof Error) {
    return <div>Error: {stream.message}</div>;
  }
  const streamUrl =
    "ws://" +
    stream.server_addr +
    `/stream?device_id=${stream.id}&token=${authData.token}`;
  return <VideoController streamUrl={streamUrl} />;
};

export default VideoPreviewPanel;
