#!/bin/bash
set -euo pipefail
JAR="backend_snapshot_v1_0_0.jar"

echo "Starting backend…"
java -jar "$JAR" > /dev/null 2>&1 &
echo "Backend started successfully."
