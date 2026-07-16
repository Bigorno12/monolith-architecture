# 🏗️ Monolith Architecture

> Production-grade **modular monolith** — Spring Boot 4 · Kotlin 2.4 · Java 25

A single deployable JAR split into three Maven modules with strict one-directional dependencies:

```
rest  ──▶  service  ──▶  persistence
```

- **rest** — HTTP layer, security (JWT + OAuth2/Keycloak), API versioning
- **service** — business logic, circuit breakers/retries, caching
- **persistence** — JPA entities, repositories, Flyway migrations

## Getting Started

**Prerequisites:** Java 25 · Maven 3.9.9 · Docker (or Podman) with Docker Compose (or Podman Compose)

```bash
# 1. Start infrastructure (MySQL, PostgreSQL, Keycloak)
cd infra

# deployment.env is required — docker-compose/podman-compose reads DB & Keycloak
# credentials from it and will fail to start without it
cp deployment.env.example deployment.env
# then fill in real values for MYSQL_*, POSTGRES_*, KEYCLOAK_* in deployment.env

docker-compose up -d
# — OR, if using Podman —
podman-compose up -d

cd ..

# 2. Set the required DB & Keycloak env vars (must match values used in infra/deployment.env),
# then build and run (dev profile — requires step 1)
export WELLDEV_URL=jdbc:mysql://localhost:3306/<MYSQL_DATABASE>
export WELLDEV_USERNAME=<MYSQL_USER>
export WELLDEV_PASSWORD=<MYSQL_PASSWORD>

mvn clean package -Pdev
java -jar rest/target/rest-1.0-SNAPSHOT.jar

# — OR — run with H2 in-memory, no Docker/Podman required
mvn clean package -Ptest
java -jar rest/target/rest-1.0-SNAPSHOT.jar
```

### Required Configuration

- `rest/src/main/resources/application-dev.properties` (dev profile, MySQL) reads
  `WELLDEV_URL`, `WELLDEV_USERNAME`, and `WELLDEV_PASSWORD` from the environment —
  set these (or add them directly to the properties file) to your DB credentials
  before running with `-Pdev`.
- `rest/src/main/resources/application.properties` reads Keycloak settings from
  `WELLDEV.KEYCLOAK.*` env vars (server URL, realm, client id/secret, admin
  credentials) — set these, or add the real values directly in the properties
  file, to match your `infra/.env` Keycloak configuration.

## Building a Container Image

The `rest` module (only) is configured with `spring-boot-maven-plugin`'s
Paketo buildpacks image builder (`monolith-architecture:latest`, Java 25 via
`BP_JVM_VERSION`); it's disabled everywhere else via `<skip>true</skip>` in the
parent `pluginManagement`. Build it with:

```bash
mvn clean package spring-boot:build-image -Pdev -pl rest -am
```

- `-pl rest -am` builds `rest` **and its module dependencies** (`service`,
  `persistence`) first, then runs `build-image` only on `rest`.
- Requires a local Docker (or Podman with the Docker socket alias) daemon —
  buildpacks build the image without a Dockerfile.
- Produces `monolith-architecture:latest` in your local image store.

## Kubernetes (kind)

`infra/k8s` provides a local blue/green deployment on [kind](https://kind.sigs.k8s.io/):

```bash
cd infra/k8s/kind
./kind-cluster.sh create      # spins up the cluster + NGINX ingress
cd ../..

kind load docker-image monolith-architecture:latest --name monolith-cluster
kubectl apply -f k8s/manifests/
```

- **Manifests** (`k8s/manifests/`): `mysql.yaml`, `postgres.yaml`, `keycloak.yaml`,
  `api.yaml` (blue/green `Deployment`s + `Service` for the API), `ingress.yaml`
  (`/` → API, `/auth` → Keycloak), `lgtm.yaml` (observability stack).
- `api.yaml` uses `imagePullPolicy: Never`, so the image must be built locally
  and loaded into the kind cluster (`kind load docker-image`) before applying.
- Run `./check-ready.sh` (inside `infra/k8s`) to verify all pods are healthy
  and auto-dump logs/describe output for any pod that isn't.
- Tear down with `./kind-cluster.sh destroy`.

## API Docs

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON spec | http://localhost:8080/v3/api-docs |
| H2 Console *(test profile only)* | http://localhost:8080/h2-console · user `sa` / no password |
