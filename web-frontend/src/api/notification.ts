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

export const PAGE_SIZE = 15;

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
  return res;
};


/**
 * Fetch a list of images related to the notification.
 * @param notificationId 
 * @returns a list of urls with images
 */
export const fetchNotificationImages = async function (
  token: string,
  notificationId: number
): Promise<string[] | HttpError> {
  const res = await fetchSafe<string[]>(
    `${ENDPOINTS.NOTIFICATIONS.GET_IMAGES}/${notificationId}`,
     addCredentials({}, token)
  );
  return res;
}
