#!/bin/bash

echo -n "Enter remote username: "
read REMOTE_USER
echo -n "Enter remote host: "
read REMOTE_HOST

# the list of files and their destinations
FILES_TO_COPY=(
    "./ws-stream/target/aarch64-unknown-linux-gnu/release/ws-stream /usr/local/bin/"
    "./init_stream_multicast.sh /usr/local/lib/sss_firmware"
    "/path/to/local/config.yaml /etc/sss_firmware/"
    "/path/to/local/systemd.service /etc/systemd/system/"
)

# Copy each file to the remote host
for FILE_ENTRY in "${FILES_TO_COPY[@]}"; do
    LOCAL_FILE=$(echo $FILE_ENTRY | awk '{print $1}')
    REMOTE_DEST=$(echo $FILE_ENTRY | awk '{print $2}')
    
    echo "Copying $LOCAL_FILE to $REMOTE_USER@$REMOTE_HOST:$REMOTE_DEST"
    scp "$LOCAL_FILE" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_DEST"

done

# Reload systemd if any services were updated
ssh "$REMOTE_USER@$REMOTE_HOST" "sudo systemctl daemon-reload"