import { fetchDevices } from "@/api/device";
import SavedFacesPanel from "@/components/facial_recognition/savedFacesPanel";
import { Separator } from "@/components/ui/separator";
import { getAuthData } from "@/lib/auth/server";
import * as Tabs from "@radix-ui/react-tabs";
import {  ShieldCheckIcon } from "lucide-react";
import { redirect } from "next/navigation";

export default async function DeviceAccessPage() {
  const auth = await getAuthData();
  if (!auth) {
    redirect("/");
  }

  const devices = await fetchDevices(auth.token);
  if (devices instanceof Error) {
    throw devices;
  }

  return (
    <div className="p-5">
      <h1 className="text-4xl font-bold mb-4 sm:text-left text-center">Manage Device Access</h1>
      <div className="flex flex-col sm:flex-row justify-start items-center gap-4 ">
        <ShieldCheckIcon className="h-12 w-12 text-blue-500 flex-shrink-0" />
        <Separator orientation="vertical" data-orientation="vertical" className="SeparatorRoot"/>
        <section>
          <p>
            You can add familiar people to your devices here. Simply take a photo or
            select an existing image and make sure your device recognizes that person.
          </p>
          <p className="mt-2">
            Make sure the image is clear and facing the front for best results.
          </p>
        </section>
      </div>

      <Separator className="mb-6 mt-3" />

      {devices.length === 0 ? (
        <p>You have no devices. Please add a device first.</p>
      ) : (
        <>
        <h1 className="text-2xl font-bold mb-4">Select your device</h1>
        <Tabs.Root
          defaultValue={String(devices[0].id)}
          className="w-full"
        >
          {/* Tabs header */}
          <div className="relative">
            <Tabs.List
              aria-label="Device selection"
              className="
                flex gap-2
                overflow-x-auto
                whitespace-nowrap
                pt-2
                pb-2
                pl-2
                -mb-px
                align-items-center
                scrollbar-thin scrollbar-thumb-muted scrollbar-track-transparent
              "
            >
              {devices.map((device) => (
                <Tabs.Trigger
                  key={device.id}
                  value={String(device.id)}
                  className="
                    shrink-0
                    rounded-full
                    px-4 py-2
                    text-sm font-medium
                    border
                    border-border
                    bg-background
                    text-muted-foreground
                    transition-colors
                    hover:bg-muted
                    data-[state=active]:bg-primary
                    data-[state=active]:text-primary-foreground
                    data-[state=active]:border-primary
                    focus:outline-none focus:ring-2 focus:ring-ring
                  "
                >
                  {device.deviceName}
                </Tabs.Trigger>
              ))}
            </Tabs.List>
          </div>

          {/* Content */}
          <div className="mt-6">
            {devices.map((device) => (
              <Tabs.Content
                key={device.id}
                value={String(device.id)}
                className="
                  rounded-2xl
                  border
                  border-border
                  p-6
                  shadow-sm
                  focus:outline-none
                "
              >
                {/* Replace with real access panel */}
                <h2 className="text-xl font-semibold mb-4">
                  Access for {device.deviceName}
                </h2>
                <SavedFacesPanel deviceId={device.id} />
              </Tabs.Content>
            ))}
          </div>
        </Tabs.Root>
        </>
      )}
    </div>
  );
}
