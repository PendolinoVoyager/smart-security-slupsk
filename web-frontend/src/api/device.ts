import { addCredentials } from "@/lib/auth/client";
import { fetchSafe, HttpError } from "./utils";
import { ENDPOINTS } from "./config";

export type DeviceEntitySimple = {
  id: number;
  address: string;
  deviceName: string;
  uuid: string;
};
export async function fetchDevices(
  token: string
): Promise<DeviceEntitySimple[] | HttpError> {
  const response = await fetchSafe<DeviceEntitySimple[]>(
    ENDPOINTS.DEVICES.LIST,
    addCredentials({}, token)
  );

  if (response instanceof HttpError) {
    console.error("Fetching devices failed:", response.message);
  }

  return response;
}

export async function fetchDeviceByUuid(
  token: string,
  uuid: string
): Promise<DeviceEntitySimple | HttpError> {
  const response = await fetchSafe<DeviceEntitySimple>(
    ENDPOINTS.DEVICES.DETAILS + uuid,
    addCredentials({}, token)
  );

  if (response instanceof HttpError) {
    console.error("Fetching devices failed:", response.message);
  }

  return response;
}
