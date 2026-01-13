#!/usr/bin/env bash
set -euo pipefail

# ---------- PROMPTS ----------
read -rp "Remote user@ip (e.g. pi@192.168.1.50): " REMOTE
read -rsp "SSH / sudo password: " SSHPASS
echo

# ---------- SSH COMMANDS ----------
SSH_CMD=(sshpass -p "$SSHPASS" ssh -o StrictHostKeyChecking=no)

REMOTE_TMP="/tmp/ws_stream_deps"
REMOTE_SCRIPT="install_deps_remote.sh"

echo "📁 Creating temp dir on remote..."
"${SSH_CMD[@]}" "$REMOTE" "mkdir -p $REMOTE_TMP"

echo "🛠️ Creating dependency install script..."

"${SSH_CMD[@]}" "$REMOTE" "cat > $REMOTE_TMP/$REMOTE_SCRIPT" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail

echo "Installing dependencies..."

export DEBIAN_FRONTEND=noninteractive

apt-get update

apt-get install -y \
  ca-certificates \
  curl \
  ffmpeg \
  iproute2 \
  iptables \
  python3-websockets \
  libgstreamer1.0-dev libgstreamer-plugins-base1.0-dev libgstreamer-plugins-bad1.0-dev \
  gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-bad \
  gstreamer1.0-plugins-ugly gstreamer1.0-libav gstreamer1.0-tools gstreamer1.0-x \
  gstreamer1.0-alsa gstreamer1.0-gl gstreamer1.0-gtk3 gstreamer1.0-qt5 gstreamer1.0-pulseaudio \

echo "✅ Dependencies installed"
EOF

"${SSH_CMD[@]}" "$REMOTE" "chmod +x $REMOTE_TMP/$REMOTE_SCRIPT"

echo "🔐 Running dependency installer (sudo)..."
"${SSH_CMD[@]}" "$REMOTE" "echo '$SSHPASS' | sudo -S $REMOTE_TMP/$REMOTE_SCRIPT"

echo "🧹 Cleaning up..."
"${SSH_CMD[@]}" "$REMOTE" "rm -rf $REMOTE_TMP"

echo "🚀 Dependency installation finished"

