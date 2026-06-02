#!/bin/bash
# ==============================================
# rollback.sh - Emergency Rollback Script
# Rolls back Kubernetes deployment to previous version
# ==============================================

set -euo pipefail

NAMESPACE="${1:-devops-app}"
DEPLOYMENT="${2:-myapp}"

echo "🔄 Rolling back $DEPLOYMENT in namespace $NAMESPACE..."

# Show current status
kubectl rollout history deployment/"$DEPLOYMENT" -n "$NAMESPACE"

# Perform rollback
kubectl rollout undo deployment/"$DEPLOYMENT" -n "$NAMESPACE"

# Wait for rollback to complete
kubectl rollout status deployment/"$DEPLOYMENT" -n "$NAMESPACE" --timeout=120s

echo "✅ Rollback complete!"
kubectl get pods -n "$NAMESPACE"
