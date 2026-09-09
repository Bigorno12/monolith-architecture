#!/bin/bash

set -euo pipefail

function _fail() {
  echo -e "\033[31mERROR: $*\033[0m" 1>&2
  exit 1
}

if ! command -v helm &> /dev/null; then
    _fail "Helm is not installed. Please install it first: brew install helm"
fi

CLUSTER_NAME="monolith-cluster"

function create() {
    echo "📦 Initializing Minikube cluster using Podman..."

    if minikube profile list 2>/dev/null | grep -q "${CLUSTER_NAME}"; then
        echo "✅ Cluster '${CLUSTER_NAME}' already exists — starting it up"
        minikube start --profile "${CLUSTER_NAME}"
    else
        echo "Creating Minikube cluster named '${CLUSTER_NAME}'..."
        minikube start --profile "${CLUSTER_NAME}" --driver=podman || _fail "Minikube start failed"
    fi

    echo -e "\n-----------------------------------------------------\n"
    echo "🌐 Installing Kubernetes Gateway API CRDs..."
    kubectl apply -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.1.0/standard-install.yaml || _fail "Failed to apply Gateway API CRDs"

    echo -e "\n-----------------------------------------------------\n"
    echo "🚪 Installing kgateway via Helm OCI..."

    # NOTE: Removed 'helm repo add' and updated to use the OCI registry format
    helm upgrade --install kgateway oci://cr.kgateway.dev/kgateway-dev/charts/kgateway \
        --namespace kgateway-system \
        --create-namespace \
        --wait || _fail "Failed to install kgateway"

    echo -e "\n"
    echo "⛵ Happy Sailing! Your Minikube cluster is ready with kgateway."
    echo ""
    echo "⚠️ IMPORTANT: Because you are on macOS with Podman,"
    echo "you must run this command in a separate terminal window to route traffic:"
    echo "minikube tunnel --profile ${CLUSTER_NAME}"
}

function destroy() {
    echo "🏴‍☠️ Destroying Minikube cluster..."
    minikube delete --profile "${CLUSTER_NAME}" || _fail "Minikube delete failed"
}

function help() {
    echo "Usage: ./minikube-cluster.sh create|destroy"
}

action="${1:-help}"

case "$action" in
    "create"|"destroy"|"help")
        $action
        ;;
    *)
        help
        ;;
esac