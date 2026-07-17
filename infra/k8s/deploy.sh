#!/bin/bash

set -euo pipefail

echo "🔒 Refreshing ConfigMaps and Secrets..."
# We delete them first just in case you changed a password and are re-running the script!
kubectl delete configmap monolith-config --ignore-not-found
kubectl delete secret monolith-secrets --ignore-not-found

# Recreate them from your .env file
kubectl create configmap monolith-config --from-env-file=config.env
kubectl create secret generic monolith-secrets --from-env-file=secret.env

echo -e "\n🗄️ Deploying Databases (MySQL & Postgres)..."
kubectl apply -f mysql.yaml
kubectl apply -f postgres.yaml

echo "⏳ Waiting for databases to initialize..."
# These commands make the script pause until the pods are actually ready!
kubectl rollout status deployment/mysql
kubectl rollout status deployment/postgres

echo -e "\n🔑 Deploying Keycloak..."
kubectl apply -f keycloak.yaml

echo "⏳ Waiting for Keycloak to initialize..."
kubectl rollout status deployment/keycloak

echo -e "\n🚀 Deploying Monolith API and Ingress Routing..."
kubectl apply -f api.yaml
kubectl apply -f ingress.yaml

echo -e "\n✅ All resources applied successfully! The API is booting up."