import { addCredentials } from "../authUtils";
import { fetchSafe, HttpError } from "../http/fetch";

const STREAMING_SERVER_URL = "http://127.0.0.1:8000/";
type StreamsResponse = {
  status: string;
  payload: string | StreamsResponseDevice[];
};

type StreamsResponseDevice = {
  id: number;
  user_id: number;
  device_name: string;
  server_addr: string;
};
/**
 * Fetch available video streams for the user, based on the credentials.
 * If user is not logged in, it should return an empty list.
 * @returns
 */
const fetchStreams = async function (): Promise<
  StreamsResponseDevice[] | Error
> {
  const reqUri = STREAMING_SERVER_URL + `streams`;
  const res = await fetchSafe<StreamsResponse>(reqUri, addCredentials({}));
  if (res instanceof HttpError) {
    return res;
  }
  if (res.status == "failure") {
    return new Error(res.payload as string);
  }
  return res.payload as StreamsResponseDevice[];
};

export default fetchStreams;