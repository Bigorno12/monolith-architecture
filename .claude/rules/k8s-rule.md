---
paths:
  - "infra/k8s/**"
---

# Kubernetes manifests: practices and open findings

Distilled from *Hands-On Java with Kubernetes* (P. Mińkowski, Leanpub edition of 2026-02-04;
"p." = the book's own page numbers) and checked against `infra/k8s/manifest/`.
**The book covers application-side practice** (probes, config, secrets, resources, GitOps).
It states it does *not* cover cluster hardening (p.110), so pod security, network policy and
availability are in [Beyond the book](#beyond-the-book-general-practice-not-from-the-text),
labelled as such.

## Fixed by this repo: don't "fix" these

- Argo CD syncs `infra/k8s/manifest` from `main` with prune + self-heal. Change git, never the live cluster.
- CI owns the image tag in `api.yaml`. Never hand-edit it.
- `monolith-secrets` is created out of band by `bootstrap-gitops.sh` from `infra/secret.env`. No secret value goes in a manifest, and never open the env file (CLAUDE.md).
- Resilience (circuit breakers, retries) is in-app (Resilience4j). The book prefers a service mesh (p.69-71), but with one API and no mesh, that is a decision to keep, not drift. Don't add Istio as a side effect.

## Practices (book) → what they mean here

| Area | Practice | Source |
|---|---|---|
| Logs | Write only to stdout/stderr, structured (JSON/ECS). Spring Boot: `logging.structured.format.console=ecs` | p.50-53 |
| Probes | **Liveness must not check dependencies** (a DB blip must not restart the pod); readiness may. Readiness is served on the **main port**, liveness need not be. Put the rest of actuator on a **separate management port** | p.53-56 |
| Probes | Use a **startup probe** for slow starters, since it holds liveness/readiness off until the app is up. Don't paper over startup with a big `initialDelaySeconds` | p.53 |
| Probes | Lower dependency timeouts (e.g. Hikari `connection-timeout=2000`) so a down dependency fails the probe fast | p.55 |
| Config | ConfigMap = non-secret, Secret = secret. `envFrom` needs a restart to see a change. A **mounted volume** updates live (and the app must reload it) | p.56-62 |
| Metrics | Expose Prometheus-format metrics and scrape them with a `ServiceMonitor` | p.62-69 |
| Resources | Always set **requests**. **Memory request = limit** (the JVM sizes its heap from the limit, never the request). A CPU limit is optional and risky: JVM startup is CPU-hungry (3 s unthrottled vs ~10 s at 500m), so if you set one, don't set it low | p.71-73, 76 |
| Resources | Paketo's memory calculator derives `-Xmx` from the limit minus metaspace, code cache and `threads × stack`. Tune with `JAVA_TOOL_OPTIONS` / `BPL_JVM_THREAD_COUNT`, then **measure** real usage | p.73-76 |
| Secrets | K8s Secrets are only base64 in etcd. Use Vault + Secrets Store CSI, External Secrets, or **Sealed Secrets** for GitOps (encrypt in git, controller decrypts) | p.130, 296-299 |
| Secrets | Least privilege: one Vault role/policy per app scoped to its own path. Rotate, and have the app reload without a restart | p.134, 139-143 |
| TLS | cert-manager issues and renews certificates (`Issuer` + `Certificate`) instead of hand-made keystores | p.110-120 |
| GitOps | One namespace per environment. **Auto-sync only the test env; stage/prod need manual sync**. Reusable chart + `values-<env>.yaml` | p.284-292 |
| Scaling | HPA only does CPU/memory. KEDA for event-driven scaling. In-place pod resize can give startup CPU, then lower the limit | p.193, 202-208 |

## Audit status (2026-10-19)

The original 19 findings were fixed in one pass. **Live** = seen working on a fresh minikube
(Kubernetes v1.37, Calico) before the podman machine's 15 GiB disk filled up.
**Validated** = accepted by `kubectl apply --dry-run=server`, not yet run.

| # | Finding | Fix | Proof |
|---|---|---|---|
| 1 | Probes passed via a 302 to Keycloak | Actuator on management port **8081**; `/actuator/health/{liveness,readiness}` are `permitAll` (`KUBERNETES_PROBE_PATHS` in `SecurityConfig`) | `mvn clean test` green. **Not yet live**: the API waited on MySQL |
| 2 | `readinessProbe` with no handler | Real handlers everywhere. The old `mysql.yaml` is rejected server-side: `must specify a handler type` | Live (Postgres, Keycloak, LGTM ready) |
| 3 | No startup probes | `startupProbe` on every workload, no `initialDelaySeconds` | Live (Keycloak's startup probe gated its liveness) |
| 4 | Actuator reachable through the Gateway | Port 8081 isn't on the Service or the `HTTPRoute` | Validated |
| 5 | Every pod got every secret | One ConfigMap per workload; secrets by `secretKeyRef`, only the keys each pod uses | Live (Keycloak and Postgres started with their own keys) |
| 6 | Plain Secret, no rotation story | Sealed Secrets controller (chart 2.20.0) in `bootstrap-gitops.sh`; `seal-secrets.sh` writes a committable SealedSecret + gitignored key backup | Controller live. **`seal-secrets.sh` not yet run** |
| 7 | No structured logging | `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs` in the API ConfigMap; `KC_LOG_CONSOLE_OUTPUT=json` | Live (API logged ECS JSON) |
| 8 | No metrics/telemetry path | `spring-boot-starter-opentelemetry`; OTLP metrics + traces → `monolith-lgtm:4318` (off by default in `application.properties`); LGTM Service exposes 4317/4318 | Build green. **Export not yet seen in Grafana** |
| 9 | `MaxRAMPercentage` vs Paketo | Removed; Paketo computes the heap | Live: `-Xmx468M` from 1 GiB, 50 threads |
| 10 | LGTM BestEffort, no explicit requests | Requests = limits everywhere; LGTM probes | Live (LGTM ready) |
| 11 | Keycloak `start-dev` on its file DB (Postgres unused) | `start` on Postgres (`KC_DB*`), hostname `http://localhost/auth` + dynamic back-channel, health on :9000 | Live (ready on Postgres) |
| 12 | Plain HTTP | **Open**: needs a hostname/certificate decision (see below) | — |
| 13 | `default` namespace, DBs as Deployments | Namespace `monolith` with PSA labels; MySQL/Postgres are StatefulSets | Validated; Postgres StatefulSet live |
| 14 | Blue/green comments contradicted the selector | Comments fixed (green live), blue at 0 replicas | Validated |
| 15 | `keycloak-realm` ConfigMap created nowhere | `bootstrap-gitops.sh` creates it from `infra/keycloak/realm-export.json` | Live (Keycloak imported the realm) |
| 16 | `check-read.sh` read the wrong columns | `$3` READY / `$4` STATUS / `$5` RESTARTS; colour reset fixed | Not run |
| 17 | Unpinned Argo CD / kgateway | Argo CD v3.5.5, kgateway v2.4.7 **+ the `kgateway-crds` chart it was missing**, Gateway API v1.6.1 | Live (kgateway + Gateway proxy running) |
| 18 | `minikube start` unsized | Calico, 7 GiB / 4 CPUs (`MINIKUBE_MEMORY`/`MINIKUBE_CPUS`) | Live |
| 19 | Secret deleted then re-created | `create --dry-run=client -o yaml \| apply` | Live |
| 20 | ConfigMap internal Keycloak URLs lacked `/auth`, and `secret.env`'s dotted keys overrode them | `/auth` added; the API no longer `envFrom`s the Secret | Validated |
| 21 | `preStop: sh -c sleep 10` on a Paketo **tiny** image (no shell) | Native `lifecycle.preStop.sleep` | Validated |

### Still open

- **The full stack doesn't fit the default podman machine.** Its 15 GiB disk filled during the test (node image, ~5 GB of workload images, Paketo builder). `podman machine stop && podman machine set --disk-size 40 && podman machine start` before `minikube-cluster.sh create`.
- **Not yet proven live:** MySQL as uid 999 with a read-only root, the API's probes on 8081, NetworkPolicies (and whether kgateway labels its proxy pods `gateway.networking.k8s.io/gateway-name`), OTLP data in Grafana, and `bootstrap-gitops.sh` / `seal-secrets.sh` end to end.
- ⚠️ **OAuth2 login issuer discovery.** `spring.security.oauth2.client.provider.*.issuer-uri` is `http://localhost/auth/...`, which a pod can't reach (`localhost` is the pod). With `lazy-initialization=true` the app starts, but the first login will probably fail discovery. Fix: explicit `authorization-uri` (browser URL) + `token-uri`/`jwk-set-uri`/`user-info-uri` (in-cluster URLs) instead of `issuer-uri`. It's an app config change that also affects docker-compose, so it needs a decision.
- **TLS (#12):** an HTTPS listener needs a hostname and certificates (cert-manager self-signed for minikube, or a real issuer). It also moves every issuer URI to `https`, which ties into the discovery item above.
- **Availability:** 1 replica until sessions are shared (Spring Session), then a PodDisruptionBudget and an HPA. Don't add a PDB at 1 replica, because it blocks node drains.
- **LGTM runs as root**, so it trips the `restricted` warning. Keycloak keeps a writable root because non-optimized `start` re-augments; a custom `kc.sh build` image would allow `--optimized` + read-only root.
- **Image updates:** Dependabot watches `infra/docker-compose.yaml` but not `infra/k8s/manifest`, so the Keycloak/MySQL/Postgres/LGTM tags here must be bumped by hand to match compose.

## Beyond the book (general practice, not from the text)

Standard Kubernetes hardening. Applied as of 2026-10-19 unless marked otherwise:

- **Pod security:** `securityContext` with `runAsNonRoot`, `allowPrivilegeEscalation: false`, `readOnlyRootFilesystem` (+ `emptyDir` for `/tmp`), `capabilities.drop: [ALL]`, `seccompProfile: RuntimeDefault`. Paketo images already run non-root. The DB and Keycloak images need testing.
- **Service accounts:** a dedicated `ServiceAccount` per workload with `automountServiceAccountToken: false`. None of these apps call the Kubernetes API.
- **NetworkPolicy:** default-deny ingress, then allow api→mysql, api→keycloak, keycloak→postgres, gateway→api/keycloak. minikube's default CNI does **not enforce** them: start it with Calico or Cilium.
- **Availability (not applied):** ≥ 2 replicas, a `PodDisruptionBudget` and an HPA, blocked on shared sessions (see Still open).
- **Admission:** Pod Security Admission labels on the namespace (applied: enforce `baseline`, warn/audit `restricted`). The book uses Kyverno (p.203), so image-signature verification of the cosign-signed GHCR image is a natural policy.
- **Supply chain (not applied):** pin by image digest where the tag isn't immutable (`mysql`, `keycloak`, `lgtm`); verify the cosign signature at admission.

## Before you commit a manifest change

1. `kubectl apply --dry-run=server -f infra/k8s/manifest/<file>` (needs the cluster).
2. Update `ARCHITECTURE.md` → deployment topology and CLAUDE.md → CI / Deployment if the shape changed.
3. Remember the order: Argo applies it on merge to `main`, with prune.
