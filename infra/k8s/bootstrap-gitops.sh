#!/usr/bin/env bash
set -euo pipefail

ARGOCD_VERSION="v3.5.5"
SEALED_SECRETS_CHART_VERSION="2.20.0"   # controller v0.40.0
NAMESPACE="monolith"

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$SCRIPT_DIR" || exit 1

SECRET_FILE="../secret.env"
REALM_FILE="../keycloak/realm-export.json"
SEALED_SECRET="manifest/monolith-secrets.sealed.yaml"
SEALING_KEY="sealed-secrets-key.yaml"   # gitignored; written by seal-secrets.sh

command -v helm >/dev/null 2>&1 || { echo "❌ helm not found (brew install helm)"; exit 1; }

echo "📁 Namespace..."
kubectl apply -f manifest/namespace.yaml

echo -e "\n🔑 Sealed Secrets controller (${SEALED_SECRETS_CHART_VERSION})..."
if [ -f "$SEALING_KEY" ]; then
  # Restore the key before the controller starts, so committed SealedSecrets still decrypt
  kubectl apply -n kube-system -f "$SEALING_KEY"
fi
helm upgrade --install sealed-secrets sealed-secrets \
  --repo https://bitnami.github.io/sealed-secrets \
  --version "$SEALED_SECRETS_CHART_VERSION" \
  --set-string fullnameOverride=sealed-secrets-controller \
  --namespace kube-system --wait

echo -e "\n🔐 Application Secret..."
if [ -f "$SEALED_SECRET" ]; then
  if [ ! -f "$SEALING_KEY" ]; then
    echo "❌ $SEALED_SECRET is committed but $SEALING_KEY is missing, so this cluster can't decrypt it."
    echo "   Restore the key file, or delete $SEALED_SECRET and run ./seal-secrets.sh again."
    exit 1
  fi
  echo "Using $SEALED_SECRET (applied by Argo CD)."
else
  [ -f "$SECRET_FILE" ] || { echo "❌ Could not find secret.env at $(pwd)/$SECRET_FILE"; exit 1; }
  # Plain Secret until seal-secrets.sh is run. The annotation lets the Sealed Secrets
  # controller take it over later instead of refusing to overwrite it.
  kubectl create secret generic monolith-secrets -n "$NAMESPACE" \
    --from-env-file="$SECRET_FILE" --dry-run=client -o yaml \
    | kubectl annotate --local -f - sealedsecrets.bitnami.com/managed=true -o yaml \
    | kubectl apply -f -
fi

echo -e "\n🏰 Keycloak realm ConfigMap..."
kubectl create configmap keycloak-realm -n "$NAMESPACE" \
  --from-file=realm-export.json="$REALM_FILE" --dry-run=client -o yaml \
  | kubectl apply --server-side --force-conflicts -f -

echo -e "\n🐙 Installing Argo CD ${ARGOCD_VERSION}..."
kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -n argocd --server-side --force-conflicts \
  -f "https://raw.githubusercontent.com/argoproj/argo-cd/${ARGOCD_VERSION}/manifests/install.yaml"

echo "⏳ Waiting for Argo CD to initialize (this can take 1-2 minutes)..."
kubectl wait --namespace argocd \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/name=argocd-server \
  --timeout=300s

echo -e "\n📄 Handing control over to Argo CD..."
kubectl apply -f argo-app.yaml

echo -e "\n✅ GitOps Bootstrap Complete!"
echo "-----------------------------------------------------"
echo "Argo CD now syncs infra/k8s/manifest from main into namespace '${NAMESPACE}':"
echo "  MySQL, Postgres, Keycloak, LGTM, the API, Gateway + NetworkPolicies"
echo "The API image is on GHCR: create ghcr-secret in '${NAMESPACE}' if the package is private (README)."
echo "-----------------------------------------------------"
echo "Argo CD UI: kubectl port-forward svc/argocd-server -n argocd 8080:443"
echo "Username: admin"
echo "Password: kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath=\"{.data.password}\" | base64 -d"
