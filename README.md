# 🏗️ Monolith Architecture using Spring Boot 4.x.x · Keycloak · Kotlin 2.4 · Java 25 · Maven

> Production-grade **modular monolith** — Spring Boot 4 · Kotlin 2.4 · Java 25

```
rest  ──▶  service  ──▶  persistence
   │            │               │
   ▼            ▼               ▼
Keycloak   JSONPlaceholder   MySQL / H2
```

- **rest** — HTTP layer, stateless JWT resource server + OAuth2/OIDC client (Keycloak), API versioning, rate limiting
- **service** — business logic, circuit breakers/retries (Resilience4j), Caffeine caching
- **persistence** — JPA entities, repositories, Flyway migrations

Deploys via **Docker Compose** (local) or **Kubernetes** (`kind`, blue/green).

## Getting Started

**Prerequisites:** Java 25 · Maven 3.9.9 · Docker (or Podman) + Compose

```bash
cd infra
cp .env.example secret.env      # fill in MYSQL_*, POSTGRES_*, KEYCLOAK_* + WELLDEV.* app config
docker-compose --env-file secret.env up -d
cd ..

# export the same DB/Keycloak values from secret.env, then:
mvn clean package -Pdev
java -jar rest/target/rest-1.0-SNAPSHOT.jar

# — OR — H2 in-memory, no Docker required
mvn clean package -Ptest
java -jar rest/target/rest-1.0-SNAPSHOT.jar
```

`application-dev.properties` needs `WELLDEV_URL`/`WELLDEV_USERNAME`/`WELLDEV_PASSWORD`;
`application.properties` needs `WELLDEV.KEYCLOAK.*` — set as env vars or directly in
the properties files, matching `infra/secret.env`.

### Troubleshooting: MySQL user/password rejected

If MySQL keeps a stale user from an old volume (e.g. wrong host or password
after changing `secret.env`):

```bash
# 1. Stop and wipe the broken database volume
docker-compose --env-file secret.env down -v
podman compose --env-file secret.env down -v

# 2. Start it back up so it creates the user with your passwords
docker-compose --env-file secret.env up -d
podman compose --env-file secret.env up -d
```

If that alone doesn't fix it, connect to MySQL and reset the user manually:

```sql
CREATE USER IF NOT EXISTS 'user'@'%' IDENTIFIED BY 'MYSQL_PASSWORD';
ALTER USER 'user'@'%' IDENTIFIED BY 'MYSQL_PASSWORD';
GRANT ALL PRIVILEGES ON MYSQL_DATABASE.* TO 'user'@'%';
FLUSH PRIVILEGES;
```

## Building a Container Image

```bash
mvn clean package spring-boot:build-image -Pdev -pl rest -am
```

Only `rest` has the Paketo buildpacks image goal enabled; requires a local Docker/Podman
daemon, no Dockerfile needed. With no `<name>` set in `rest/pom.xml`, the image defaults
to `${project.artifactId}:${project.version}` → **`rest:1.0-SNAPSHOT`**. OCI labels
(title, description, source, version, authors) are populated from the project's `pom.xml`
metadata via `BP_OCI_*` env vars. To match what `infra/k8s/manifest/api.yaml` pulls
(`ghcr.io/bigorno12/monolith-architecture:latest`), tag/push explicitly:

```bash
mvn clean package spring-boot:build-image -Pdev -pl rest -am \
  -Dspring-boot.build-image.imageName=ghcr.io/bigorno12/monolith-architecture:latest \
  -Dspring-boot.build-image.publish=true
```

CI builds/publishes to GHCR the same way via the shared pipeline template — see CI/CD.

## Kubernetes (kind)

```bash
cd infra/k8s/kind && ./kind-cluster.sh create && cd ..
cp ../.env.example config.env && cp ../.env.example secret.env   # fill in real values
kubectl create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io --docker-username=<gh-user> --docker-password=<gh-pat>
cd manifest && ../bootstrap-gitops.sh
```

- `manifest/` — `mysql.yaml`, `postgres.yaml`, `keycloak.yaml`, `api.yaml` (blue/green
  Deployments + Service), `ingress.yaml` (`/` → API, `/auth` → Keycloak), `lgtm.yaml`
  (Grafana OTel-LGTM observability).
- `deploy.sh` rebuilds the `monolith-config`/`monolith-secrets` ConfigMap/Secret from
  `config.env`/`secret.env`, then rolls out MySQL → Postgres → Keycloak → API/Ingress.
- `api.yaml` pulls `ghcr.io/bigorno12/monolith-architecture:latest` (`imagePullPolicy: Always`)
  via the `ghcr-secret` image pull secret — push an image to GHCR first (see above).
- `./check-read.sh` (in `infra/k8s`) reports pod health and dumps logs for failures.
- Tear down: `kind/kind-cluster.sh destroy`.

## CI/CD

`.github/workflows/ci.yml` delegates the whole pipeline (build → lint → unit/integration
tests → CodeQL/Gitleaks security scan → Docker image build/publish to GHCR) to a shared
reusable workflow, [`Bigorno12/ci-cd-templates`](https://github.com/Bigorno12/ci-cd-templates).
`auto-release.yml` tags/releases on every merge to `main` (patch bump, keeps latest 10
releases). `dependabot.yml` updates Maven, Docker Compose, and Actions weekly/monthly.

## API Docs

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON spec | http://localhost:8080/v3/api-docs |
| H2 Console *(test profile only)* | http://localhost:8080/h2-console · user `sa` / no password |
