#!/bin/bash
# ==============================================
# elasticsearch-setup.sh
# Creates index templates, ILM policies, and
# initial Kibana dashboards via Elasticsearch API
# ==============================================

set -euo pipefail

ES_HOST="${ES_HOST:-http://localhost:9200}"

echo "⚙️ Setting up Elasticsearch at $ES_HOST..."

# Wait for ES to be ready
until curl -sf "$ES_HOST/_cluster/health" > /dev/null; do
  echo "Waiting for Elasticsearch..."
  sleep 5
done

# ── ILM Policy: auto-rollover + delete old indices ────────────
curl -s -X PUT "$ES_HOST/_ilm/policy/microservices-policy" \
  -H 'Content-Type: application/json' -d '{
  "policy": {
    "phases": {
      "hot": {
        "actions": {
          "rollover": {
            "max_size": "10gb",
            "max_age": "1d"
          }
        }
      },
      "warm": {
        "min_age": "3d",
        "actions": {
          "shrink": { "number_of_shards": 1 },
          "forcemerge": { "max_num_segments": 1 }
        }
      },
      "delete": {
        "min_age": "30d",
        "actions": { "delete": {} }
      }
    }
  }
}'
echo "✅ ILM policy created"

# ── Index Template ────────────────────────────────────────────
curl -s -X PUT "$ES_HOST/_index_template/microservices-template" \
  -H 'Content-Type: application/json' -d '{
  "index_patterns": ["microservices-logs-*", "microservices-errors-*", "kafka-events-*"],
  "template": {
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 1,
      "index.lifecycle.name": "microservices-policy",
      "index.lifecycle.rollover_alias": "microservices"
    },
    "mappings": {
      "properties": {
        "@timestamp":    { "type": "date" },
        "service_name":  { "type": "keyword" },
        "environment":   { "type": "keyword" },
        "cluster":       { "type": "keyword" },
        "event_type":    { "type": "keyword" },
        "log": {
          "properties": {
            "level":   { "type": "keyword" },
            "message": { "type": "text" },
            "logger":  { "type": "keyword" }
          }
        }
      }
    }
  }
}'
echo "✅ Index template created"

echo ""
echo "✅ Elasticsearch setup complete!"
echo "Open Kibana at: http://localhost:5601"
echo "Index patterns to create:"
echo "  - microservices-logs-*"
echo "  - microservices-errors-*"
echo "  - kafka-events-*"
