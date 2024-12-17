#!/bin/bash

BINARY_NAME="ws-stream"  # Replace with the name of your binary
TARGET_USER="sssdev"    # Replace with the username for the target device
TARGET_DIR="/usr/bin"       # Target directory on the device

if [ $# -eq 0 ];
then
  echo "$0: Missing arguments: provide IP to deploy to"
  exit 1
fi
TARGET_IP=$1

BUILD_PATH="target/aarch64-unknown-linux-gnu/release/$BINARY_NAME"

# Ensure the binary exists
if [ ! -f "$BUILD_PATH" ]; then
    echo "Error: Binary $BUILD_PATH does not exist. Did you run cargo build?"
    exit 1
fi

echo "Copying $BINARY_NAME to $TARGET_USER@$TARGET_IP:$TARGET_DIR ..."
scp "$BUILD_PATH" "$TARGET_USER@$TARGET_IP:$TARGET_DIR/$BINARY_NAME"

if [ $? -eq 0 ]; then
    echo "Successfully copied $BINARY_NAME to $TARGET_IP:$TARGET_DIR."
else
    echo "Error: Failed to copy the binary. Check your network and SSH setup."
    exit 1
fi

# Ensure the binary has executable permissions on the target device
echo "Setting executable permissions on $TARGET_IP:$TARGET_DIR/$BINARY_NAME ..."
ssh "$TARGET_USER@$TARGET_IP" "sudo chmod +x $TARGET_DIR/$BINARY_NAME"


if [ $? -eq 0 ]; then
    echo "Successfully set executable permissions for $BINARY_NAME."
    echo "Done! You can now run the binary on the target device."
else
    echo "Error: Failed to set permissions. Check your SSH and sudo setup."
    exit 1
fi