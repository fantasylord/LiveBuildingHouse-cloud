#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_BIN="${DOCKER_BIN:-docker}"
IMAGE_PREFIX="${IMAGE_PREFIX:-}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
PUSH="${PUSH:-false}"
NO_CACHE="${NO_CACHE:-false}"
PLATFORM="${PLATFORM:-}"
EXTRA_BUILD_ARGS="${EXTRA_BUILD_ARGS:-}"

if [[ -n "${IMAGE_PREFIX}" && "${IMAGE_PREFIX}" != */ ]]; then
  IMAGE_PREFIX="${IMAGE_PREFIX}/"
fi

build_args=()

if [[ -n "${PLATFORM}" ]]; then
  build_args+=(--platform "${PLATFORM}")
fi

if [[ "${NO_CACHE}" == "true" ]]; then
  build_args+=(--no-cache)
fi

if [[ -n "${EXTRA_BUILD_ARGS}" ]]; then
  read -r -a extra_args <<< "${EXTRA_BUILD_ARGS}"
  build_args+=("${extra_args[@]}")
fi

build_image() {
  local image_name="$1"
  local context_dir="$2"
  local full_image="${IMAGE_PREFIX}${image_name}:${IMAGE_TAG}"

  echo "Building ${full_image}"
  "${DOCKER_BIN}" build "${build_args[@]}" -t "${full_image}" "${ROOT_DIR}/${context_dir}"

  if [[ "${PUSH}" == "true" ]]; then
    echo "Pushing ${full_image}"
    "${DOCKER_BIN}" push "${full_image}"
  fi
}

build_image "livehouse-api" "livehouse/livehouse-api"
build_image "livehouse-admin" "livehouse/livehouse-admin"
build_image "livehouse-user" "livehouse/livehouse-user"

echo "Done."
