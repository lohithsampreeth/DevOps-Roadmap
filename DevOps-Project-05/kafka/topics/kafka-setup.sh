#!/bin/bash
# ==============================================
# kafka-setup.sh - Create all Kafka topics
# Run once after Kafka is running
# ==============================================

set -euo pipefail

KAFKA_BROKER="${KAFKA_BROKER:-localhost:9094}"

echo "🔧 Setting up Kafka topics at $KAFKA_BROKER..."

create_topic() {
  local topic=$1
  local partitions=${2:-3}
  local replication=${3:-1}
  local retention=${4:-604800000}   # 7 days default

  kafka-topics.sh \
    --bootstrap-server "$KAFKA_BROKER" \
    --create \
    --if-not-exists \
    --topic "$topic" \
    --partitions "$partitions" \
    --replication-factor "$replication" \
    --config retention.ms="$retention" \
    --config cleanup.policy=delete

  echo "✅ Created topic: $topic (partitions=$partitions)"
}

# ── Core Event Topics ─────────────────────────────────────────
create_topic "order-events"        3 1 604800000    # 7 days
create_topic "inventory-events"    3 1 604800000
create_topic "notification-events" 3 1 259200000    # 3 days
create_topic "payment-events"      3 1 604800000

# ── Dead Letter Queue Topics ──────────────────────────────────
create_topic "order-events-dlq"        1 1 2592000000   # 30 days
create_topic "inventory-events-dlq"    1 1 2592000000
create_topic "notification-events-dlq" 1 1 2592000000

# ── Audit & Metrics Topics ────────────────────────────────────
create_topic "audit-events"      1 1 7776000000    # 90 days
create_topic "metrics-events"    1 1 86400000      # 1 day

echo ""
echo "📋 All topics created:"
kafka-topics.sh --bootstrap-server "$KAFKA_BROKER" --list

echo ""
echo "✅ Kafka setup complete!"
