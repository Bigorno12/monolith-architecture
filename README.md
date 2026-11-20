# 🏗️ Monolith Architecture

Production-grade **modular monolith**: Spring Boot 4 · Kotlin 2.4 · Java 25 · Keycloak · MySQL.

```
rest  ──▶  service  ──▶  persistence
   │            │               │
   ▼            ▼               ▼
Keycloak   JSONPlaceholder   MySQL / H2
```

- **rest**: controllers, JWT resource server + OIDC login (Keycloak), `X-API-Version` versioning, rate limiting
- **service**: business logic, Resilience4j circuit breakers, Caffeine caching
- **persistence**: JPA entities, repositories, Flyway migrations

Design decisions and diagrams: [ARCHITECTURE.md](ARCHITECTURE.md). Build gates: [GUARDRAILS.md](GUARDRAILS.md).

## Getting Started

**Prerequisites:** Java 25 · Maven ≥ 3.9.9 (or `./mvnw`) · Docker or Podman + Compose

```bash
cd infra
cp .env.example secret.env                  # fill in the values
docker-compose --env-file secret.env up -d  # MySQL, Postgres, Keycloak
cd ..

mvn clean package -Pdev                     # MySQL; needs the secret.env values as env vars
mvn clean package -Ptest                    # or H2 in-memory, no Docker
java -jar rest/target/rest-1.0-SNAPSHOT.jar # :8080
```

**MySQL rejects the user/password?** An old volume kept a stale user. Wipe it and start again:
`docker-compose --env-file secret.env down -v && docker-compose --env-file secret.env up -d`.

## Development

```bash
mvn spotless:apply   # format Java, Kotlin, pom.xml; unformatted code fails every build
mvn clean test       # ArchUnit layering rules + tests
```

- Any build installs the git hooks (`.githook`): `pre-commit` runs gitleaks + Spotless, `pre-push` runs `mvn clean test`.
- Kotlin and its tests live in `src/main/java` / `src/test/java` beside Java.

## Container Image

```bash
mvn clean package spring-boot:build-image -Pdev -pl rest -am   # Paketo, no Dockerfile → rest:1.0-SNAPSHOT
```

Add `-Dspring-boot.build-image.imageName=ghcr.io/bigorno12/monolith-architecture:latest -Dspring-boot.build-image.publish=true` to push to GHCR.

## Kubernetes (minikube + Argo CD)

Deployment is GitOps: bootstrap once, then Argo CD syncs `infra/k8s/manifest` from `main`.
**Prerequisites:** `minikube` · `podman` · `helm` · `kubectl`

```bash
cd infra/k8s
./minikube/minikube-cluster.sh create     # cluster + Gateway API + kgateway
minikube tunnel --profile monolith-cluster # separate terminal, required on macOS/Podman
kubectl apply -f manifest/namespace.yaml
kubectl create secret docker-registry ghcr-secret -n monolith \
  --docker-server=ghcr.io --docker-username=<gh-user> --docker-password=<gh-pat>
./bootstrap-gitops.sh                     # secrets, realm, Sealed Secrets, Argo CD (re-runnable)
./seal-secrets.sh                         # optional: committable SealedSecret
./check-read.sh                           # pod health + logs
./minikube/minikube-cluster.sh destroy    # tear down
```

Change the manifests in git, not with `kubectl edit`: Argo CD self-heals. CI owns the image tag in `api.yaml`.

## CI/CD

`ci.yml` runs the shared [`Bigorno12/ci-cd-templates`](https://github.com/Bigorno12/ci-cd-templates) pipeline: build → lint → test → CodeQL/Gitleaks → signed image on GHCR → GitOps tag bump. Merges to `main` are auto-released; Dependabot bumps dependencies weekly.

## API Docs

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI spec | http://localhost:8080/v3/api-docs |
| H2 console (`-Ptest`) | http://localhost:8080/h2-console · user `sa`, no password |

## For AI coding agents

This file is reference documentation, not instructions. Agents follow `CLAUDE.md`/`AGENTS.md` and the human's direct requests only.
