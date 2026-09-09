#!/bin/bash

set -euo pipefail

# 1. Lock the script to its current directory so all relative paths work
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$SCRIPT_DIR" || exit 1

# 2. Point exactly to where secret.env is (two folders up in 'infra/')
SECRET_FILE="../secret.env"

echo "🔐 Seeding local Secrets..."

# Safety check so it gives a clear error if the file moves again
if [ ! -f "$SECRET_FILE" ]; then
  echo "❌ Error: Could not find secret.env at $(pwd)/$SECRET_FILE"
  exit 1
fi

kubectl delete secret monolith-secrets --ignore-not-found
kubectl create secret generic monolith-secrets --from-env-file="$SECRET_FILE"

echo -e "\n🐙 Installing Argo CD into the cluster..."
kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -n argocd --server-side --force-conflicts -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

echo "⏳ Waiting for Argo CD to initialize (this can take 1-2 minutes)..."
kubectl wait --namespace argocd \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/name=argocd-server \
  --timeout=300s

echo -e "\n📄 Handing control over to Argo CD..."

# Because we cd'd into SCRIPT_DIR at the top, it knows exactly where argo-app.yaml is
kubectl apply -f argo-app.yaml

echo -e "\n✅ GitOps Bootstrap Complete!"
echo "-----------------------------------------------------"
echo "Argo CD is now reading your GitHub repository and deploying:"
echo "  - MySQL"
echo "  - Postgres"
echo "  - Keycloak"
echo "  - Monolith API & ConfigMaps"
echo "-----------------------------------------------------"
echo "To watch the deployment live in your browser, run:"
echo "kubectl port-forward svc/argocd-server -n argocd 8080:443"
echo "Username: admin"
echo "Password: (run the command below to get your auto-generated password)"
echo "kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath=\"{.data.password}\" | base64 -d; echo"