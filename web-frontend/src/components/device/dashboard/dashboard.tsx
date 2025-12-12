import Image from "next/image";
import { DeviceEntitySimple } from "@/api/device";
import VideoPreviewPanel from "../video-preview/videoPreviewPanel";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { env } from "node:process";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import { Button } from "@/components/ui/button";
import NotificationListPaginated from "../notification/notificationListPaginated";
import { WeatherStats } from "./weatherStats";
import AudioRecorder from "../audioRecorder";
import { getAuthData } from "@/lib/auth/client";

// Panel with device info and image
function DeviceInfoPanel({ device }: { device: DeviceEntitySimple }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Device Info</CardTitle>
        <WeatherStats deviceId={device.id} />
      </CardHeader>
      <CardContent>
        <div className="space-y-2 text-sm">
          <div>
            <strong>Name:</strong> {device.deviceName}
          </div>
          <Collapsible>
            <CollapsibleTrigger asChild>
              <Button variant="outline">Show address</Button>
            </CollapsibleTrigger>
            <CollapsibleContent>
              <div>
                <strong>Address:</strong> {device.address}
              </div>
            </CollapsibleContent>
          </Collapsible>
          {env.NODE_ENV !== "production" ? (
            <>
              <div>
                <strong>UUID:</strong> {device.uuid}
              </div>
              <div>
                <strong>ID:</strong> {device.id}
              </div>
            </>
          ) : (
            ""
          )}
        </div>
        <div className="mt-4">
          <Image
            src={`/devices/prototype.jpg`}
            alt={`${device.deviceName} placeholder`}
            width={300}
            height={200}
            className="rounded-md border"
          />
        </div>
      </CardContent>
    </Card>
  );
}

type DeviceDashboardProps = {
  device: DeviceEntitySimple;
};

export default  function DeviceDashboard({ device }: DeviceDashboardProps) {
  const authData = getAuthData();
  const token = authData?.token!;

  return (
    <div className="flex flex-col gap-4 p-4">
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Device Info */}
        <div className="lg:col-span-1">
          <DeviceInfoPanel device={device} />
        </div>

        {/* Live Feed */}
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>Live Feed - {device.deviceName}</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-col md:flex-row justify-center items-center md:items-start gap-4">
                <VideoPreviewPanel deviceId={device.id} />
                <AudioRecorder deviceId={device.id} token={token} />
              </div>
            </CardContent>



          </Card>
        </div>
      </div>

      {/* Notifications */}
      <NotificationListPaginated deviceUuid={device.uuid} />
    </div>
  );
}
