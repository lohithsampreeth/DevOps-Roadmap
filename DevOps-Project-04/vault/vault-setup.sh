#!/bin/bash
# ==============================================
# vault-setup.sh
# Sets up Vault secrets, policies, and K8s auth
# for the Inventory Manager app
# Run once after Vault is initialized
# ==============================================

set -euo pipefail

VAULT_ADDR="${VAULT_ADDR:-http://vault:8200}"
VAULT_TOKEN="${VAULT_ROOT_TOKEN}"
K8S_NAMESPACE="inventory-prod"
APP_ROLE="inventory-app"

echo "🔐 Setting up Vault at $VAULT_ADDR..."

export VAULT_ADDR VAULT_TOKEN

# ── 1. Enable KV secrets engine ─────────────────────────
echo "Enabling KV secrets engine..."
vault secrets enable -path=secret kv-v2 || echo "Already enabled"

# ── 2. Store app secrets ─────────────────────────────────
echo "Writing application secrets..."
vault kv put secret/inventory-app \
  db_url="jdbc:postgresql://postgres-svc:5432/inventorydb" \
  db_user="inventory_user" \
  db_pass="$(openssl rand -base64 24)" \
  app_secret="$(openssl rand -base64 32)"

echo "✅ Secrets stored at secret/inventory-app"

# ── 3. Create Vault policy ───────────────────────────────
echo "Creating Vault policy..."
vault policy write inventory-app-policy - << 'POLICY'
# Allow app to read its own secrets
path "secret/data/inventory-app" {
  capabilities = ["read"]
}

# Allow app to renew its own token
path "auth/token/renew-self" {
  capabilities = ["update"]
}

# Allow app to look up its own token
path "auth/token/lookup-self" {
  capabilities = ["read"]
}
POLICY

# ── 4. Enable Kubernetes auth method ────────────────────
echo "Enabling Kubernetes auth..."
vault auth enable kubernetes || echo "Already enabled"

# Get K8s cluster info from running pod
K8S_HOST="https://kubernetes.default.svc"
K8S_CA=$(kubectl config view --raw --minify --flatten \
  -o jsonpath='{.clusters[].cluster.certificate-authority-data}' | base64 -d)

vault write auth/kubernetes/config \
  kubernetes_host="$K8S_HOST" \
  kubernetes_ca_cert="$K8S_CA"

# ── 5. Create Kubernetes auth role ──────────────────────
echo "Creating Kubernetes auth role..."
vault write auth/kubernetes/role/$APP_ROLE \
  bound_service_account_names=inventory-app-sa \
  bound_service_account_namespaces=$K8S_NAMESPACE \
  policies=inventory-app-policy \
  ttl=1h

echo ""
echo "✅ Vault setup complete!"
echo ""
echo "Summary:"
echo "  Secret path : secret/inventory-app"
echo "  Policy      : inventory-app-policy"
echo "  K8s role    : $APP_ROLE"
echo "  Namespace   : $K8S_NAMESPACE"
echo ""
echo "Verify with:"
echo "  vault kv get secret/inventory-app"
