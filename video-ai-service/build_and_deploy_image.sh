#!/usr/bin/env bash
set -euo pipefail

# =========================
# CONFIG — CHANGE THESE
# =========================

DOCKER_ORG="cinnablossom"
DOCKER_REPO="smart-intercom"
IMAGE_NAME="ai-service"
RELEASE="1.0.1"

# =========================
# ARGUMENT PARSING
# =========================

if [[ $# -ne 1 ]]; then
  echo "❌ Usage: $0 <cpu|gpu>"
  exit 1
fi

TARGET="$1"

case "$TARGET" in
  cpu)
    DOCKERFILE="Dockerfile.cpu"
    TAG_SUFFIX="cpu"
    ;;
  gpu)
    DOCKERFILE="Dockerfile.gpu"
    TAG_SUFFIX="gpu"
    ;;
  *)
    echo "❌ Invalid argument: $TARGET"
    echo "   Allowed values: cpu | gpu"
    exit 1
    ;;
esac

FULL_IMAGE="${DOCKER_ORG}/${DOCKER_REPO}:${IMAGE_NAME}-${TAG_SUFFIX}-${RELEASE}"

# =========================
# BUILD & PUSH
# =========================

echo "🔨 Building image (${TARGET}): ${FULL_IMAGE}"
docker build \
  -f "${DOCKERFILE}" \
  -t "${FULL_IMAGE}" \
  .

echo "📤 Pushing image: ${FULL_IMAGE}"
docker push "${FULL_IMAGE}"

echo "✅ Done: ${FULL_IMAGE}"

