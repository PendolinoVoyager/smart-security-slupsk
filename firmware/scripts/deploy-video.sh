#!/usr/bin/env bash
set -euo pipefail

# ---------- FILES ----------
FILES_TO_COPY=(
  "../ws-stream/target/aarch64-unknown-linux-gnu/release/ws-stream"
  "../init_stream_multicast.sh"
  "../index.html"
  "../hls_server.py"
  "../audioListener.py"
)

REMOTE_TMP="/tmp/ws_stream_deploy"
REMOTE_INSTALL_SCRIPT="install_remote.sh"

# ---------- PROMPTS ----------
read -rp "Remote user@ip (e.g. pi@192.168.1.50): " REMOTE
read -rsp "SSH / sudo password: " SSHPASS
echo

# ---------- PRECHECKS ----------
for f in "${FILES_TO_COPY[@]}"; do
  [[ -f "$f" ]] || { echo "❌ Missing file: $f"; exit 1; }
done

# ---------- SSH COMMANDS ----------
SSH_CMD=(sshpass -p "$SSHPASS" ssh -o StrictHostKeyChecking=no)
SCP_CMD=(sshpass -p "$SSHPASS" scp -o StrictHostKeyChecking=no)

echo "📁 Creating temp dir on remote..."
"${SSH_CMD[@]}" "$REMOTE" "mkdir -p $REMOTE_TMP"

echo "📤 Uploading files..."
"${SCP_CMD[@]}" "${FILES_TO_COPY[@]}" "$REMOTE:$REMOTE_TMP/"

echo "🛠️ Creating remote install script..."

"${SSH_CMD[@]}" "$REMOTE" "cat > $REMOTE_TMP/$REMOTE_INSTALL_SCRIPT" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail

HTTP_DIR="/var/localstream/http"
HLS_DIR="$HTTP_DIR/stream"

NON_ROOT_USER="${SUDO_USER:-$USER}"
NON_ROOT_GROUP="$(id -gn "$NON_ROOT_USER")"

echo "Installing as root..."

mkdir -p "$HTTP_DIR" "$HLS_DIR"

chown -R "$NON_ROOT_USER:$NON_ROOT_GROUP" "$HTTP_DIR"
chmod 755 "$HTTP_DIR"
chmod 775 "$HLS_DIR"

install -m 755 /tmp/ws_stream_deploy/ws-stream /usr/sbin/ws-stream
install -m 755 /tmp/ws_stream_deploy/init_stream_multicast.sh /usr/sbin/init_stream_multicast.sh
install -m 555 /tmp/ws_stream_deploy/hls_server.py /usr/sbin/hls_server.py
install -m 555 /tmp/ws_stream_deploy/audioListener.py /usr/sbin/audioListener.py
chown root:root /usr/sbin/ws-stream /usr/sbin/init_stream_multicast.sh

install -m 644 /tmp/ws_stream_deploy/index.html "$HTTP_DIR/index.html"
chown "$NON_ROOT_USER:$NON_ROOT_GROUP" "$HTTP_DIR/index.html"

echo "✅ Installation complete"
EOF

"${SSH_CMD[@]}" "$REMOTE" "chmod +x $REMOTE_TMP/$REMOTE_INSTALL_SCRIPT"

echo "🔐 Running remote installer (sudo)..."
"${SSH_CMD[@]}" "$REMOTE" "echo '$SSHPASS' | sudo -S $REMOTE_TMP/$REMOTE_INSTALL_SCRIPT"

echo "🧹 Cleaning up..."
"${SSH_CMD[@]}" "$REMOTE" "rm -rf $REMOTE_TMP"

echo "🚀 Deployment finished successfully"

