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

# .env is required — docker-compose/podman-compose reads DB & Keycloak
# credentials from it and will fail to start without it
cp .env.example .env
# then fill in real values for MYSQL_*, POSTGRES_*, KEYCLOAK_* in .env

docker-compose up -d
# — OR, if using Podman —
podman-compose up -d

cd ..

# 2. Set the required DB & Keycloak env vars (must match values used in infra/.env),
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

## API Docs

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON spec | http://localhost:8080/v3/api-docs |
| H2 Console *(test profile only)* | http://localhost:8080/h2-console · user `sa` / no password |
