#!/usr/bin/env bash

set -e

SECURE=false

# Parse flags
while [[ "$1" == --* ]]; do
  case "$1" in
    --secure|--ssl)
      SECURE=true
      shift
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

if [ $# -ne 1 ]; then
  echo "Usage: $0 [--secure|--ssl] <IP_OR_HOSTNAME>"
  exit 1
fi

IP="$1"
ENV_FILE=".env"

if [ "$SECURE" = true ]; then
  HTTP_PROTO="https"
  WS_PROTO="wss"
else
  HTTP_PROTO="http"
  WS_PROTO="ws"
fi

cat > "$ENV_FILE" <<EOF
# these are for PRODUCTION BUILD ONLY! See default development values in related source code files
NEXT_PUBLIC_BACKEND_URL=${HTTP_PROTO}://${IP}:8080
NEXT_PUBLIC_STREAMING_SERVER_HTTP_URL=${HTTP_PROTO}://${IP}:9002
NEXT_PUBLIC_STREAMING_SERVER_WS_URL=${WS_PROTO}://${IP}:9080
NEXT_PUBLIC_AUDIO_SERVER_URL=${WS_PROTO}://${IP}:8888
EOF

echo "✔ Generated $ENV_FILE for $IP ($HTTP_PROTO / $WS_PROTO)"

