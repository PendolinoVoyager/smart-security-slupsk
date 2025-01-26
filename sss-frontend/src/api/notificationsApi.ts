import { addCredentials, getToken } from "../authUtils";
import { fetchSafe } from "../http/fetch";

const ENDPOINT = "http://localhost:8080/api/v1/notification/";

export type NotificationResponse = {
  type: string;
  message: string;
  has_seen: string;
};

const fetchNotifications = async function (
  device: number
): Promise<NotificationResponse[] | Error> {
  const token = getToken();
  if (!token) {
    return new Error("not logged in");
  }
  const res = await fetchSafe<NotificationResponse[]>(
    ENDPOINT + device.toString(),
    addCredentials({})
  );
  return res;
};

export default fetchNotifications;
