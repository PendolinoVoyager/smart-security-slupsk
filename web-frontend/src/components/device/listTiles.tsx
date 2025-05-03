import Image from "next/image";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import Link from "next/link";
import { DeviceEntitySimple } from "@/api/device";

export default function DeviceListTiles({
  devices,
}: {
  devices: DeviceEntitySimple[];
}) {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
      {devices.map((device) => (
        <Link href={`devices/${device.uuid}`} key={device.id}>
          <Card className="overflow-hidden shadow-md hover:shadow-lg transition-shadow">
            <CardHeader className="p-0">
              <Image
                src="/devices/prototype.jpg"
                alt={device.deviceName}
                width={400}
                height={200}
                className="object-cover w-full h-40"
              />
            </CardHeader>
            <CardContent className="p-4">
              <CardTitle className="text-lg">{device.deviceName}</CardTitle>
              <p className="text-sm text-muted-foreground">
                UUID: {device.uuid}
              </p>
              <p className="text-sm text-muted-foreground">
                Address: {device.address}
              </p>
            </CardContent>
          </Card>
        </Link>
      ))}
    </div>
  );
}
