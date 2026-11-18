#!/usr/bin/env bash
set -euo pipefail

LOG_FILE=/var/log/sanmarcos/heartbeat.log
URL=${HEARTBEAT_URL:-http://localhost/actuator/health}
TIMESTAMP=$(date -u +%Y-%m-%dT%H:%M:%SZ)

mkdir -p "$(dirname "$LOG_FILE")"

if curl -fsS --max-time 5 "$URL" >/dev/null 2>&1; then
  printf '%s HEARTBEAT OK url=%s\n' "$TIMESTAMP" "$URL" >> "$LOG_FILE"
else
  printf '%s HEARTBEAT FAIL url=%s\n' "$TIMESTAMP" "$URL" >> "$LOG_FILE"
fi
