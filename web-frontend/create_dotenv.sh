#!/usr/bin/env bash

set -e

if [ $# -ne 1 ]; then
  echo "Usage: $0 <IP_OR_HOSTNAME>"
  exit 1
fi

IP="$1"
ENV_FILE=".env"

cat > "$ENV_FILE" <<EOF
# these are for PRODUCTION BUILD ONLY! See default development values in related source code files
NEXT_PUBLIC_BACKEND_URL=http://${IP}:8080
NEXT_PUBLIC_STREAMING_SERVER_HTTP_URL=http://${IP}:9002
NEXT_PUBLIC_STREAMING_SERVER_WS_URL=ws://${IP}:9080
NEXT_PUBLIC_AUDIO_SERVER_URL=ws://${IP}:8888
EOF

echo "✔ Generated $ENV_FILE with IP: $IP"
