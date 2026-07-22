#!/bin/bash

set -euo pipefail

echo "🔐 Seeding local Secrets..."

kubectl delete secret monolith-secrets --ignore-not-found
kubectl create secret generic monolith-secrets --from-env-file=secret.env

echo -e "\n🐙 Installing Argo CD into the cluster..."
kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

echo "⏳ Waiting for Argo CD to initialize (this can take 1-2 minutes)..."
kubectl wait --namespace argocd \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/name=argocd-server \
  --timeout=300s

echo -e "\n📄 Handing control over to Argo CD..."

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