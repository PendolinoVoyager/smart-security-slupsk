#!/usr/bin/env bash
set -euo pipefail

# =========================
# CONFIG — CHANGE THESE
# =========================

DOCKER_ORG="cinnablossom"
# Docker Hub username or org
DOCKER_REPO="smart-intercom"

# Image name (per service)
IMAGE_NAME="iot-backend"

# Release tag (shared across all services)
RELEASE="1.0.1"


# =========================
# SCRIPT
# =========================

FULL_IMAGE="${DOCKER_ORG}/${DOCKER_REPO}:${IMAGE_NAME}-${RELEASE}"

echo "🔨 Building image: ${FULL_IMAGE}"
docker build -t "${FULL_IMAGE}" .

echo "📤 Pushing image: ${FULL_IMAGE}"
docker push "${FULL_IMAGE}"

echo "✅ Done: ${FULL_IMAGE}"

