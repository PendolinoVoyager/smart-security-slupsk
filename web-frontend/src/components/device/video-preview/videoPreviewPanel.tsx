"use server";
import { FC } from "react";
import { getStreamAvailability } from "@/api/stream";
import { getAuthData } from "@/lib/auth/server";
import VideoController from "./videoController";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { ExclamationTriangleIcon } from "@radix-ui/react-icons";
import {ENDPOINTS} from "@/api/config";
import Image from "next/image";
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
    let errorMessage = "";
    try {
      const streamJson = JSON.parse(stream.message);
      console.error(streamJson);
      errorMessage = streamJson?.payload ?? "fatal unknown error";
    } catch (e) {
      errorMessage = `Unknown Error: ${stream.message}`;
    } finally {
      return (
        <Alert variant="destructive" className="mt-4">
          <ExclamationTriangleIcon className="h-5 w-5" />
          <AlertTitle>Stream Unavailable</AlertTitle>
          <AlertDescription>
            {errorMessage}
            <br />
            The device may not be connected or configured correctly.
            <div className="ml-auto mr-auto">
              <Image src="/devices/camera-off.png" alt="Stream error" width={400} height={300} className="mt-4 w-50" loading="lazy" />
            </div>
          </AlertDescription>
        </Alert>
      );
    }
  }
  
  const streamUrl =
   ENDPOINTS.STREAMING.WATCH_STREAM + 
    `?device_id=${stream.id}&token=${authData.token}`;
  return <VideoController streamUrl={streamUrl} />;
};

export default VideoPreviewPanel;
