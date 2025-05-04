export const AUTH_PROVIDER_URL = "http://127.0.0.1:8080";
export const BACKEND_PROVIDER_URL = "http://localhost:8080";
export const STREAMING_SERVER_HTTP_URL = "http://localhost:9000";
export const STREAMING_SERVER_WS_URL = "ws://localhost:9080";
export const ENDPOINTS = {
  AUTH: {
    REGISTER: `${AUTH_PROVIDER_URL}/api/v1/auth/register`,
    LOGIN: `${AUTH_PROVIDER_URL}/api/v1/auth/login`,
    ACTIVATE: `${AUTH_PROVIDER_URL}/api/v1/auth/activation-token/verify`,
    RESET: `${AUTH_PROVIDER_URL}/api/v1/auth/reset-password`,
  },
  DEVICES: {
    LIST: `${BACKEND_PROVIDER_URL}/api/v1/device/`,
    DETAILS: `${BACKEND_PROVIDER_URL}/api/v1/device/`,
  },
  STREAMING: {
    /**
     * Fetch all available streams from streaming server.
     */
    GET_STREAMS: `${STREAMING_SERVER_HTTP_URL}/streams`,
    /**
     * Check if the stream is available and get its details if applicable.
     * Params:
     *  - device_id - number
     */
    GET_STREAM_AVAILABILITY: `${STREAMING_SERVER_HTTP_URL}/streams/availability`,
    /** Preview the stream from the device.
     *  After making a succesfull connection to this WebSocket endpoint,
     *  stream data will be sent in complete packets to this socket.
     *
     * Params:
     * - device_id - number
     * - token - string
     */
    WATCH_STREAM: `${STREAMING_SERVER_WS_URL}/stream`,
  },
};
