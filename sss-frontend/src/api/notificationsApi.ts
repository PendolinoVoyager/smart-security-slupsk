import { addCredentials, getToken } from "../authUtils";
import { fetchSafe } from "../http/fetch";

const ENDPOINT = "http://192.168.10.47/api/v1/notification";
const PAGE_SIZE = 15;

export type NotificationResponse = {
  page: number;
  total: number;
  notifications: NotificationResponseItem[];
};

export type NotificationResponseItem = {
  id: number;
  type: string;
  message: string;
  has_seen: boolean;
  timestamp: string;
};

const fetchNotifications = async function (
  page: number
): Promise<NotificationResponse | Error> {
  const token = getToken();
  if (!token) {
    return new Error("not logged in");
  }
  const res = await fetchSafe<NotificationResponse>(
    `${ENDPOINT}?page=${page}&size=${PAGE_SIZE}`,
    addCredentials({})
  );
  console.log(res);
  return res;
};

export default fetchNotifications;
