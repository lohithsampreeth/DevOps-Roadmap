#!/bin/bash
# monitor.sh - CPU, Memory, Disk alert script

THRESHOLD_CPU=80
THRESHOLD_MEM=85
THRESHOLD_DISK=90
EMAIL="you@example.com"

CPU=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1)
MEM=$(free | grep Mem | awk '{print $3/$2 * 100.0}')
DISK=$(df / | tail -1 | awk '{print $5}' | cut -d'%' -f1)

if (( $(echo "$CPU > $THRESHOLD_CPU" | bc -l) )); then
  echo "ALERT: CPU usage is ${CPU}%" | mail -s "CPU Alert" $EMAIL
fi

if (( $(echo "$MEM > $THRESHOLD_MEM" | bc -l) )); then
  echo "ALERT: Memory usage is ${MEM}%" | mail -s "Memory Alert" $EMAIL
fi

if [ "$DISK" -gt "$THRESHOLD_DISK" ]; then
  echo "ALERT: Disk usage is ${DISK}%" | mail -s "Disk Alert" $EMAIL
fi

echo "CPU: ${CPU}% | MEM: ${MEM}% | DISK: ${DISK}%"
