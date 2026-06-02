#!/bin/bash
# ==============================================
# cleanup.sh - Remove old Docker images & ECR tags
# Run weekly via cron to save disk & registry space
# ==============================================

set -euo pipefail

ECR_REPO="${ECR_REPOSITORY:-}"
KEEP_LAST=10

echo "🧹 Cleaning up old Docker images locally..."
docker image prune -af --filter "until=168h"   # older than 7 days

if [ -n "$ECR_REPO" ]; then
  echo "🧹 Cleaning up old ECR images (keeping last $KEEP_LAST)..."
  aws ecr describe-images \
    --repository-name "$ECR_REPO" \
    --query 'sort_by(imageDetails,& imagePushedAt)[:-'"$KEEP_LAST"'].imageDigest' \
    --output text | \
  while read -r digest; do
    echo "Deleting: $digest"
    aws ecr batch-delete-image \
      --repository-name "$ECR_REPO" \
      --image-ids imageDigest="$digest"
  done
fi

echo "✅ Cleanup complete!"
