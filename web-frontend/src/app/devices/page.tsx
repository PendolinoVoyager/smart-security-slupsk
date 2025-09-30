import { fetchDevices } from "@/api/device";
import DeviceListTiles from "@/components/device/listTiles";
import { Separator } from "@/components/ui/separator";
import { getAuthData } from "@/lib/auth/server";
import { redirect } from "next/navigation";

export default async function DevicesPage() {
  const auth = await getAuthData();
  if (!auth) {
    redirect("/");
  }

  const devices = await fetchDevices(auth.token);
  if (devices instanceof Error) {
    throw devices;
  }

  return (
    <div className="p-6">
      <h1 className="text-4xl font-bold mb-4">Your devices</h1>
      <Separator className="mb-6" />
      <DeviceListTiles devices={devices} />
    </div>
  );
}
