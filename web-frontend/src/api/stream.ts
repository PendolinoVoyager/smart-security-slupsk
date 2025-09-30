import { addCredentials } from "@/lib/auth/client";
import { fetchSafe, HttpError } from "./utils";
import { ENDPOINTS } from "./config";

type StreamsResponse = {
  status: string;
  payload: string | { count: number; available: StreamsResponseDevice[] };
};

type StreamsResponseDevice = {
  id: number;
  user_id: number;
  device_name: string;
  server_addr: string;
};
/**
 * Fetch available video streams for the user, based on the credentials.
 * If user is not logged in, it should return an error.
 * @param token string whee
 */
export const getStreams = async function (
  token: string
): Promise<StreamsResponseDevice[] | Error> {
  const res = await fetchSafe<StreamsResponse>(
    ENDPOINTS.STREAMING.GET_STREAMS,
    addCredentials({}, token)
  );
  if (res instanceof HttpError) {
    return res;
  }
  if (res.status == "failure") {
    return new Error(res.payload as string);
  }
  return (
    ((res.payload as { count: number; available: StreamsResponseDevice[] })
      .available as StreamsResponseDevice[]) ?? []
  );
};

export const getStreamAvailability = async function (
  token: string,
  deviceId: number
): Promise<StreamsResponseDevice | Error> {
  const res = await fetchSafe<StreamsResponse>(
    ENDPOINTS.STREAMING.GET_STREAM_AVAILABILITY + `?device_id=${deviceId}`,
    addCredentials({}, token)
  );
  if (res instanceof HttpError) {
    return res;
  }
  if (res.status == "failure") {
    return new Error(res.payload as string);
  }
  if (typeof res.payload !== "string" && res.payload.count === 1) {
    return res.payload.available[0];
  } else {
    return new HttpError("device not ready", 404);
  }
};
