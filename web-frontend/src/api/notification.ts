import { addCredentials } from "@/lib/auth/client";
import { ENDPOINTS } from "./config";
import { fetchSafe, HttpError } from "./utils";

export type Notification = {
  id: number;
  type: string;
  message: string;
  has_seen: boolean;
  /**
   * ISO string probally but not sure
   */
  timestamp: string;
};

const PAGE_SIZE = 15;

export type NotificationResponse = {
  page: number;
  total: number;
  notifications: Notification[];
};
/**
 *
 * @param page Page starting from 0
 * @returns
 */
export const fetchByUserPaginated = async function (
  token: string,
  page: number
): Promise<NotificationResponse | HttpError> {
  const res = await fetchSafe<NotificationResponse>(
    `${ENDPOINTS.NOTIFICATIONS.BY_DEVICE_PAGINATED}?page=${page}&size=${PAGE_SIZE}`,
    addCredentials({}, token)
  );
  console.debug(res);
  return res;
};
