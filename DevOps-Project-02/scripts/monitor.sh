#!/bin/bash
# ==============================================
# monitor.sh - Server Health Monitor & Alerting
# Checks CPU, Memory, Disk, and running services
# ==============================================

set -euo pipefail

# ---------- Config ----------
THRESHOLD_CPU=80
THRESHOLD_MEM=85
THRESHOLD_DISK=90
ALERT_EMAIL="you@example.com"
LOG_FILE="/var/log/devops-monitor.log"
SLACK_WEBHOOK="${SLACK_WEBHOOK_URL:-}"    # optional

# ---------- Colors ----------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# ---------- Logging ----------
log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

alert() {
  local msg="$1"
  log "ALERT: $msg"

  # Email alert
  if command -v mail &>/dev/null; then
    echo "$msg" | mail -s "🚨 Server Alert: $(hostname)" "$ALERT_EMAIL"
  fi

  # Slack alert (if webhook configured)
  if [ -n "$SLACK_WEBHOOK" ]; then
    curl -s -X POST "$SLACK_WEBHOOK" \
      -H 'Content-type: application/json' \
      --data "{\"text\":\"🚨 *Server Alert* on \`$(hostname)\`\n$msg\"}" \
      > /dev/null
  fi
}

# ---------- CPU Check ----------
check_cpu() {
  local cpu
  cpu=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1 | cut -d'.' -f1)
  if [ "$cpu" -gt "$THRESHOLD_CPU" ]; then
    alert "CPU usage is ${cpu}% (threshold: ${THRESHOLD_CPU}%)"
    echo -e "${RED}CPU: ${cpu}%${NC}"
  else
    echo -e "${GREEN}CPU: ${cpu}% ✓${NC}"
  fi
}

# ---------- Memory Check ----------
check_memory() {
  local mem
  mem=$(free | grep Mem | awk '{printf "%.0f", $3/$2 * 100.0}')
  if [ "$mem" -gt "$THRESHOLD_MEM" ]; then
    alert "Memory usage is ${mem}% (threshold: ${THRESHOLD_MEM}%)"
    echo -e "${RED}MEM: ${mem}%${NC}"
  else
    echo -e "${GREEN}MEM: ${mem}% ✓${NC}"
  fi
}

# ---------- Disk Check ----------
check_disk() {
  while IFS= read -r line; do
    local usage mount
    usage=$(echo "$line" | awk '{print $5}' | cut -d'%' -f1)
    mount=$(echo "$line" | awk '{print $6}')
    if [ "$usage" -gt "$THRESHOLD_DISK" ]; then
      alert "Disk usage on $mount is ${usage}% (threshold: ${THRESHOLD_DISK}%)"
      echo -e "${RED}DISK $mount: ${usage}%${NC}"
    else
      echo -e "${GREEN}DISK $mount: ${usage}% ✓${NC}"
    fi
  done < <(df -h | tail -n +2 | grep -v tmpfs)
}

# ---------- Service Check ----------
check_services() {
  local services=("docker" "kubelet" "nginx")
  for svc in "${services[@]}"; do
    if systemctl is-active --quiet "$svc" 2>/dev/null; then
      echo -e "${GREEN}SERVICE $svc: running ✓${NC}"
    else
      alert "Service $svc is NOT running!"
      echo -e "${RED}SERVICE $svc: NOT running${NC}"
    fi
  done
}

# ---------- Main ----------
log "===== Health Check Started ====="
check_cpu
check_memory
check_disk
check_services
log "===== Health Check Complete ====="
