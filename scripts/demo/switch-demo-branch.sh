#!/usr/bin/env bash
set -euo pipefail

STEP="${1:-}"
FORCE="${2:-}"

case "$STEP" in
  10) TARGET="demo/10-hashmap-racy" ;;
  20) TARGET="demo/20-sync" ;;
  30) TARGET="demo/30-concurrent-hashmap" ;;
  40) TARGET="demo/40-jmm-threadsafe" ;;
  50) TARGET="demo/50-db-racy" ;;
  60) TARGET="demo/60-db-safe" ;;
  *)
    echo "Usage: $0 {10|20|30|40|50|60} [--force]"
    exit 1
    ;;
esac

if [[ "$FORCE" != "--force" ]] && [[ -n "$(git status --porcelain)" ]]; then
  echo "Working tree is not clean. Commit or stash first, or rerun with --force."
  exit 1
fi

git checkout "$TARGET"
echo "Switched to $TARGET"
