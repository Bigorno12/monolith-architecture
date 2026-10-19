#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="monolith"
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$SCRIPT_DIR" || exit 1

SECRET_FILE="../secret.env"
SEALED_SECRET="manifest/monolith-secrets.sealed.yaml"
SEALING_KEY="sealed-secrets-key.yaml"

command -v kubeseal >/dev/null 2>&1 || { echo "❌ kubeseal not found (brew install kubeseal)"; exit 1; }
[ -f "$SECRET_FILE" ] || { echo "❌ Could not find secret.env at $(pwd)/$SECRET_FILE"; exit 1; }

kubectl create secret generic monolith-secrets -n "$NAMESPACE" \
  --from-env-file="$SECRET_FILE" --dry-run=client -o yaml \
  | kubeseal --format yaml \
      --controller-namespace kube-system --controller-name sealed-secrets-controller \
  > "$SEALED_SECRET"

umask 077
kubectl get secret -n kube-system -l sealedsecrets.bitnami.com/sealed-secrets-key -o yaml > "$SEALING_KEY"

echo "✅ Wrote $SEALED_SECRET: commit it."
echo "🔒 Backed up the sealing key to $SEALING_KEY: keep it outside git (it is gitignored)."
