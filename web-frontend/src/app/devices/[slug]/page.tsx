import { fetchDeviceByUuid } from "@/api/device";
import { getAuthData } from "@/lib/auth/server";
import { Params } from "next/dist/server/request/params";
import { redirect } from "next/navigation";
function isUuidV4(uuid: string): boolean {
  const r = new RegExp(
    "/^[0-9A-F]{8}-[0-9A-F]{4}-[4][0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}$/i"
  );
  return r.test(uuid);
}
export default async function DevicePage({
  params,
}: {
  params: Promise<Params>;
}) {
  const auth = await getAuthData();
  if (!auth) {
    redirect("/");
  }
  const { slug } = await params;
  // if (!isUuidV4(slug)) {
  //   throw new Error("Invalid device UUID");
  // }
  const device = await fetchDeviceByUuid(auth.token, slug as string);
  console.log(device);
  if (device instanceof Error) {
    throw device;
  }
  return <p>Post: {device.toString()}</p>;
}
