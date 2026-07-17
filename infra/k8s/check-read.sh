#!/usr/bin/env bash
set -euo pipefail

# Align to the k8s directory specifically
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$SCRIPT_DIR" || exit 1

fail() { echo -e "\033[31mERROR: $*\033[0M" >&2; exit 1; }
info() { echo -e "\033[36mINFO: $*\033[0m"; }
success() { echo -e "\033[32mSUCCESS: $*\033[0m"; }

command -v kubectl >/dev/null 2>&1 || fail "kubectl not found in PATH"

# Removed --short as it is deprecated in newer kubectl version
info "kubectl version:"
kubectl version --client || true

# Look for kind config dynamically from the script's directory, fallback to current K8s context
if [[ -f kind/kind-config.yml ]]; then
  CLUSTER_NAME=$(awk '/^\s*name:\s*/{print $2; exit}' kind/kind-config.yml || true)
else
  CLUSTER_NAME=$(kubectl config current-context 2>/dev/null || echo "kind")
fi

info "Active Cluster / Context: ${CLUSTER_NAME}"

info "Nodes:"
kubectl get nodes -o wide || true

info "Pods (all namespaces):"
kubectl get pods -A -o wide || true

info "Pods not in Running phase (if any):"
kubectl get pods -A --field-selector=status.phase!=Running || echo "(none)"

info "Pods with restart count > 0 (if any):"
kubectl get pods -A --no-headers | awk '$4+0>0 {print $0}' || echo "(none)"

# Collect list of suspect pods (not Running or restart >0)
mapfile -t suspect < <(kubectl get pods -A --no-headers | awk '$4+0>0 || $4 ~ /0\// || $3!~/Running/ {print $1"/"$2}' | sort -u || true)

if [[ ${#suspect[@]} -eq 0 ]]; then
  success "No suspect pods detected. Your infrastructure is healthy!"
  exit 0
fi

info "Inspecting ${#suspect[@]} suspect pods for diagnostics..."
for p in "${suspect[@]}"; do
  ns=${p%%/*}
  name=${p#*/}
  echo
  echo -e "\033[33m--- Pod: ${ns}/${name} ---\033[0m"
  kubectl describe pod "$name" -n "$ns" || true
  echo
  echo -e "\033[33m--- Logs (current) for ${name} ---\033[0m"
  kubectl logs "$name" -n "$ns" --all-containers || true
  echo
  echo -e "\033[33m--- Logs (previous) for ${name} (if any) ---\033[0m"
  kubectl logs "$name" -n "$ns" --all-containers --previous || echo "(no previous logs)"
done

exit 0