#!/usr/bin/env bash
set -e

SECURE=false

# Default ports (can be overridden or disabled)
BACKEND_PORT=8080
STREAM_HTTP_PORT=9002
STREAM_WS_PORT=9080
AUDIO_WS_PORT=8888

# Parse flags
while [[ "$1" == --* ]]; do
  case "$1" in
    --secure|--ssl)
      SECURE=true
      shift
      ;;
    --backend-port=*)
      BACKEND_PORT="${1#*=}"
      shift
      ;;
    --stream-http-port=*)
      STREAM_HTTP_PORT="${1#*=}"
      shift
      ;;
    --stream-ws-port=*)
      STREAM_WS_PORT="${1#*=}"
      shift
      ;;
    --audio-ws-port=*)
      AUDIO_WS_PORT="${1#*=}"
      shift
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

if [ $# -ne 1 ]; then
  echo "Usage:"
  echo "  $0 [--secure] [--backend-port=PORT] [--stream-http-port=PORT] [--stream-ws-port=PORT] [--audio-ws-port=PORT] <IP_OR_HOSTNAME>"
  echo
  echo "Use an empty value to omit a port (proxy mode):"
  echo "  --backend-port="
  exit 1
fi

IP="$1"
ENV_FILE=".env"

# Protocols
if [ "$SECURE" = true ]; then
  HTTP_PROTO="https"
  WS_PROTO="wss"
else
  HTTP_PROTO="http"
  WS_PROTO="ws"
fi

# Helper to append port only if set
with_port() {
  local host="$1"
  local port="$2"

  if [ -n "$port" ]; then
    echo "${host}:${port}"
  else
    echo "${host}"
  fi
}

cat > "$ENV_FILE" <<EOF
# these are for PRODUCTION BUILD ONLY! See default development values in related source code files
NEXT_PUBLIC_BACKEND_URL=${HTTP_PROTO}://$(with_port "$IP" "$BACKEND_PORT")
NEXT_PUBLIC_STREAMING_SERVER_HTTP_URL=${HTTP_PROTO}://$(with_port "$IP" "$STREAM_HTTP_PORT")
NEXT_PUBLIC_STREAMING_SERVER_WS_URL=${WS_PROTO}://$(with_port "$IP" "$STREAM_WS_PORT")
NEXT_PUBLIC_AUDIO_SERVER_URL=${WS_PROTO}://$(with_port "$IP" "$AUDIO_WS_PORT")
EOF

echo "✔ Generated $ENV_FILE for $IP ($HTTP_PROTO / $WS_PROTO)"

