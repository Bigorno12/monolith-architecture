#!/bin/bash

set -euo pipefail

function _fail() {
  echo -e "\033[31mERROR: $*\033[0m" 1>&2
  exit 1
}

CLUSTER_NAME="monolith-cluster"
GATEWAY_API_VERSION="v1.6.1"
KGATEWAY_VERSION="v2.4.7"
MINIKUBE_MEMORY="${MINIKUBE_MEMORY:-7g}"
MINIKUBE_CPUS="${MINIKUBE_CPUS:-4}"

function create() {
    command -v helm &> /dev/null || _fail "Helm is not installed. Please install it first: brew install helm"

    echo "📦 Initializing Minikube cluster using Podman..."

    if minikube profile list 2>/dev/null | grep -q "${CLUSTER_NAME}"; then
        echo "✅ Cluster '${CLUSTER_NAME}' already exists — starting it up"
        echo "   (--cni/--memory/--cpus apply only when a cluster is created; destroy + create to change them)"
        minikube start --profile "${CLUSTER_NAME}"
    else
        echo "Creating Minikube cluster named '${CLUSTER_NAME}'..."
        minikube start --profile "${CLUSTER_NAME}" --driver=podman --cni=calico \
            --memory="${MINIKUBE_MEMORY}" --cpus="${MINIKUBE_CPUS}" || _fail "Minikube start failed"
    fi

    echo -e "\n-----------------------------------------------------\n"
    echo "🌐 Installing Kubernetes Gateway API CRDs ${GATEWAY_API_VERSION}..."
    kubectl apply --server-side -f "https://github.com/kubernetes-sigs/gateway-api/releases/download/${GATEWAY_API_VERSION}/standard-install.yaml" \
        || _fail "Failed to apply Gateway API CRDs"

    echo -e "\n-----------------------------------------------------\n"
    echo "🚪 Installing kgateway ${KGATEWAY_VERSION} via Helm OCI..."
    helm upgrade --install kgateway-crds oci://cr.kgateway.dev/kgateway-dev/charts/kgateway-crds \
        --version "${KGATEWAY_VERSION}" \
        --namespace kgateway-system \
        --create-namespace || _fail "Failed to install kgateway CRDs"
    helm upgrade --install kgateway oci://cr.kgateway.dev/kgateway-dev/charts/kgateway \
        --version "${KGATEWAY_VERSION}" \
        --namespace kgateway-system \
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
