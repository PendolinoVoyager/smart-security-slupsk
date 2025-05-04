import { DeviceEntitySimple } from "@/api/device";
import React from "react";
import VideoPreviewPanel from "../video-preview/videoPreviewPanel";

type DeviceDashboardProps = {
  device: DeviceEntitySimple;
};
export default function DeviceDashboard({ device }: DeviceDashboardProps) {
  return <VideoPreviewPanel deviceId={device.id} />;
}
