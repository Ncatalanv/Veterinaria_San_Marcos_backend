#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

sudo mkdir -p /var/log/sanmarcos
sudo install -m 0755 "$SCRIPT_DIR/heartbeat.sh" /usr/local/bin/sanmarcos-heartbeat.sh
sudo install -m 0644 "$SCRIPT_DIR/sanmarcos-heartbeat.service" /etc/systemd/system/sanmarcos-heartbeat.service
sudo install -m 0644 "$SCRIPT_DIR/sanmarcos-heartbeat.timer" /etc/systemd/system/sanmarcos-heartbeat.timer
sudo systemctl daemon-reload
sudo systemctl enable --now sanmarcos-heartbeat.timer

if ! command -v amazon-cloudwatch-agent-ctl >/dev/null 2>&1; then
  wget -q -O /tmp/amazon-cloudwatch-agent.deb https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
  sudo dpkg -i /tmp/amazon-cloudwatch-agent.deb
fi

sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config -m ec2 -s \
  -c "file:$SCRIPT_DIR/cloudwatch-agent-config.json"

sudo systemctl status sanmarcos-heartbeat.timer --no-pager
