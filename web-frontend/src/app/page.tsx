import { fetchDevices } from "@/api/device";
import HomePagePromo from "./homePagePromo";
import { getAuthData } from "@/lib/auth/server";
import DeviceDashboard from "@/components/device/dashboard/dashboard";

export default async function Home() {
  const authData = await getAuthData();
  const loggedIn = !!authData;
  if (loggedIn) {
    const devices = await fetchDevices(authData?.token);
    if (devices instanceof Error) {
      throw devices;
    }
    if (devices.length === 0) {
      return <h1>You don't have any devices yet!</h1>;
    }
    return <DeviceDashboard device={devices[0]} />;
  } else return <HomePagePromo />;
}
