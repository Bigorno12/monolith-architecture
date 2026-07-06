# 🏗️ Monolith Architecture

> Production-grade **modular monolith** — Spring Boot 4 · Kotlin 2.4 · Java 25

---

## What is a Monolith Architecture?

A **monolith** is a single deployable unit where all application concerns — business logic, data access, and HTTP handling — live in the same codebase and are built and deployed together.

```
┌─────────────────────────────────┐
│         Single Process          │
│  ┌──────┐ ┌─────────┐ ┌──────┐ │
│  │  API │ │Business │ │  DB  │ │
│  │Layer │ │ Logic   │ │Layer │ │
│  └──────┘ └─────────┘ └──────┘ │
└─────────────────────────────────┘
         One deployable JAR
```

**Advantages:**
- Simple to develop, test, and deploy
- No network overhead between layers (in-process calls)
- Easy ACID transactions across the whole domain
- Lower operational complexity compared to microservices

**Disadvantages:**
- Full redeploy required for any change
- Can become harder to maintain as the codebase grows
- Scaling requires replicating the entire application, not just individual hot spots

### Modular Monolith (this project)

A **modular monolith** preserves single-deployment simplicity while enforcing **clear internal boundaries** via Maven modules. Each module has its own package scope and can only access what it explicitly declares as a dependency — the same separation you'd get with microservices, without the distributed systems complexity.

```
┌──────────────────────────────────────────────┐
│              Single Deployable JAR           │
│  ┌─────────┐   ┌─────────┐   ┌───────────┐  │
│  │  rest   │──▶│ service │──▶│persistence│  │
│  │ module  │   │ module  │   │  module   │  │
│  └─────────┘   └─────────┘   └───────────┘  │
│   (no reverse dependencies allowed)          │
└──────────────────────────────────────────────┘
```

The build enforces this via Maven Enforcer rules: `requirePluginVersions`, `dependencyConvergence`, and `requireUpperBoundDeps` run on every build to guarantee consistency.

---

## Architecture

Three Maven modules with a strict one-directional dependency flow:

```
rest  ──▶  service  ──▶  persistence
 │              │               │
 ▼              ▼               ▼
Keycloak   JSONPlaceholder   MySQL / H2
```

### Module Breakdown

#### `persistence` — `mu.server.persistence`
The data layer. No business logic lives here.

| Package | Content |
|---|---|
| `entity` | JPA entities: `User`, `Todo`, `Post`, `Comment` |
| `repository` | Spring Data repositories + Blaze Persistence custom repos |
| `repository.blaze` | Entity views (`UserView`, `TodoView`) for efficient projections |
| `audit` | `Auditable` base class + `AuditorAwareImpl` |
| `converter` | `EncryptionConverter` — JPA attribute converter for AES-256 |
| `enumeration` | `Role`, `Permission`, `Gender`, `TokenType` |

#### `service` — `mu.server.service`
The business logic layer. Depends on `persistence`. Contains no HTTP concerns.

| Package | Content |
|---|---|
| `service` | Service interfaces: `UserService`, `TodoService`, `KeycloakService` |
| `service.impl` | Implementations: `UserServiceImpl`, `TodoServiceImpl`, `KeycloakServiceImpl` |
| `service.http` | `JsonPlaceHolderService` — declarative HTTP client via `@HttpExchange` |
| `dto` | Request/response DTOs (auth, user, todo) |
| `mapper` | MapStruct mappers: `UserMapper`, `TodoMapper`, `KeycloakMapper` |
| `exception` | Typed exceptions: `NotFoundException`, `UsernameExistException`, etc. |

#### `rest` — `mu.server.rest`
The entry point and HTTP layer. Depends on `service`. Contains all Spring Boot configuration.

| Package | Content |
|---|---|
| `controller` | REST controllers: `AuthenticationControllerV2`, `UserController`, `TodoController`, `AdminController` |
| `config` | Spring configuration classes (Security, Keycloak, Blaze, Async, Cache, REST client) |
| `filter` | `FingerprintFilter`, `RateLimitFilter` |
| `advice` | `GlobalExceptionHandler` — centralised error responses |

---

## Tech Stack

| Category | Technology | Version |
|---|---|---|
| Runtime | Java | 25 |
| Language | Kotlin + Java (mixed) | Kotlin 2.4.0 |
| Framework | Spring Boot | 4.0.6 |
| Build | Maven | 3.9.9 |
| ORM | Spring Data JPA / Hibernate | Boot-managed |
| Advanced Queries | Blaze Persistence | 1.6.18 |
| DB Migration | Flyway | Boot-managed |
| Database (dev) | MySQL | 8.4 |
| Database (test) | H2 in-memory | Boot-managed |
| Identity Provider | Keycloak | 26.0.9 / 26.6.1 |
| Security | Spring Security OAuth2 RS + Client | Boot-managed |
| Caching | Caffeine | Boot-managed |
| Rate Limiting | Bucket4j | 8.19.0 |
| Fault Tolerance | Resilience4j (circuit breakers) + Spring `@Retryable` | 2.4.0 |
| Object Mapping | MapStruct | 1.6.3 |
| Boilerplate | Lombok | Boot-managed |
| API Docs | SpringDoc / Swagger UI | 3.0.3 |
| HTTP Client | Spring RestClient `@HttpExchange` | Boot-managed |
| Keycloak Admin | Admin Client + RESTEasy | 26.0.9 / 7.0.2.Final |
| Concurrency | Java Virtual Threads | Java 25 |
| Infra | Docker Compose | — |

### Mixed Kotlin + Java Codebase

This project intentionally uses both Kotlin and Java within the same Maven modules. Kotlin is used for concise service interfaces, controllers, and filters; Java is used for configuration classes, JPA entities, and mappers. Both languages compile via the Kotlin Maven plugin (with `spring` all-open compiler plugin) and share the same annotation processor path for Lombok and MapStruct.

---

## Domain Model

```
User ──< Todo
User ──< Post ──< Comment
```

| Entity | Fields |
|---|---|
| `User` | `id`, `firstname`, `lastname`, `age`, `gender`, `username`, `email` *(AES-256 encrypted)*, `keycloakId`, `role` + audit fields |
| `Todo` | `id`, `title`, `completed`, `user_id (FK → User)` |
| `Post` | `id`, `title`, `body`, `user_id (FK → User)` |
| `Comment` | `id`, `email`, `name`, `body`, `post_id (FK → Post)` |

All entities extend `Auditable` which adds four JPA auditing fields: `createdDate`, `lastModifiedDate`, `createdBy`, and `modifiedBy`. The auditor is populated from the Spring Security context via `AuditorAwareImpl`.

### Encryption

The `email` field on `User` is transparently encrypted at rest using a JPA `AttributeConverter` (`EncryptionConverter`). The converter calls `AESConverter`, which applies AES-256 encryption/decryption on read and write — no application code needs to handle raw cipher text.

---

## Security

The application registers **two independent `SecurityFilterChain` beans**, ordered by priority.

### Chain 1 — Stateless JWT Resource Server (`@Order(1)`)

Matches: `/api/v1/mono/**`

```
Incoming request
      │
      ▼
BearerTokenAuthenticationFilter   ──  validates Keycloak JWT signature & expiry
      │
      ▼
FingerprintFilter                  ──  anti-hijacking: hashes IP + User-Agent,
      │                                compares against cached fingerprint per token.
      │                                Rejects with 401 if fingerprint changed.
      ▼
RateLimitFilter                    ──  Bucket4j token bucket: 100 req/min.
      │                                Responds with HTTP 429 when exhausted.
      ▼
Controller / Method-level @PreAuthorize
```

Session creation policy is `STATELESS`. CSRF is disabled for this chain.

### Chain 2 — OAuth2 OIDC Client (`@Order(2)`)

Matches: all other paths

- Full OIDC login flow via Keycloak (`authorization_code` grant)
- Logout endpoint at `/api/v1/auth/logout` — invalidates the HTTP session, deletes `JSESSIONID` and `XSRF-TOKEN` cookies, and triggers an OIDC back-channel logout via `OidcClientInitiatedLogoutSuccessHandler`
- CSRF protection is enabled with `CookieCsrfTokenRepository`
- MFA is enabled via Spring Security's `@EnableMultiFactorAuthentication` (password + OTT factors)

### CORS

Both chains share a single `CorsConfigurationSource`:

| Setting | Value |
|---|---|
| Allowed origins | `http://localhost:8080`, `http://localhost:4200` |
| Allowed methods | `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS` |
| Allowed headers | `Authorization`, `Cache-Control`, `Content-Type`, `X-API-Version`, `X-XSRF-TOKEN` |
| Allow credentials | `true` |

### Roles & Permissions

| Role | Authorities |
|---|---|
| `USER` | `user:create`, `user:read`, `user:update`, `user:delete`, `admin:read` |
| `ADMIN` | `admin:create`, `admin:read`, `admin:update`, `admin:delete` |

Authorities are extracted from the Keycloak JWT using a custom `KeycloakAuthenticationConverter`, which reads the `realm_access.roles` claim and maps it to Spring Security `GrantedAuthority` objects.

---

## Keycloak Integration

Keycloak is the identity provider. The application interacts with it in two ways:

1. **Token validation** — The resource server validates JWTs against Keycloak's JWKS endpoint (`/realms/monolith-keycloak/protocol/openid-connect/certs`) and checks the issuer URI.
2. **Admin operations** — `KeycloakServiceImpl` uses the `keycloak-admin-client` (backed by RESTEasy) to programmatically register users, assign roles, and issue tokens on behalf of users. This is used by the register and login endpoints.

On startup, Keycloak automatically imports the realm configuration from the `infra/keycloak/` directory via the `--import-realm` flag.

---

## Resilience — Circuit Breakers & Retries

The project uses **Resilience4j** (`resilience4j-spring-boot3`) for fault tolerance around the two least-reliable external dependencies: **Keycloak** (admin API calls) and the internal **user update path**.

### Circuit Breakers (`UserServiceImpl`)

| Method | Circuit Breaker Name | Fallback Method | Purpose |
|---|---|---|---|
| `UserServiceImpl.updateUser()` | `userService` | `fallbackUpdateUser(request, ex)` — logs the error | Protects the user-update transaction path |
| `UserServiceImpl.deleteUser()` | `keycloakService` | `fallbackDeleteUser(username, ex)` — logs the error | Protects calls to the Keycloak Admin Client (`UsersResource.delete`) |

Both instances share the same threshold configuration, defined in `rest/src/main/resources/application.properties`:

| Property | Value | Meaning |
|---|---|---|
| `sliding-window-type` | `count_based` | Failure rate is computed over a fixed number of calls |
| `sliding-window-size` | `10` | Last 10 calls are evaluated |
| `minimum-number-of-calls` | `5` | Circuit won't trip until at least 5 calls have been recorded |
| `failure-rate-threshold` | `50` (%) | Circuit opens if ≥ 50% of the last window's calls failed |
| `wait-duration-in-open-state` | `10s` | Time before transitioning from `OPEN` → `HALF_OPEN` |
| `permitted-number-of-calls-in-half-open-state` | `3` | Trial calls allowed while `HALF_OPEN` before deciding to close or re-open |
| `register-health-indicator` | `true` | Exposes circuit state via `/actuator/health` |
| `event-consumer-buffer-size` | `10` | Ring buffer size for circuit breaker event history |

`keycloakService` additionally sets `ignore-exceptions=mu.server.service.exception.NotFoundException`, so a legitimate "user not found" doesn't count as a circuit-tripping failure.

### Retry (`JsonPlaceHolderService`)

The declarative HTTP client `JsonPlaceHolderService.todo()` is annotated with Spring's `@Retryable` (not Resilience4j) for transient failures against the external JSONPlaceholder API:

| Setting | Value |
|---|---|
| `maxRetries` | 4 |
| `delay` | 2000 ms initial delay |
| `multiplier` | ×2.0 backoff |
| `maxDelay` | 4000 ms cap |

### Authentication is unchanged

⚠️ Note: the stateless **JWT Bearer-token resource server chain via Keycloak is still active** and has **not** been removed — it remains `@Order(1)` in `SecurityConfig`, matching `/api/v1/mono/**`, with `FingerprintFilter` and `RateLimitFilter` chained after `BearerTokenAuthenticationFilter`. If the intent is to move away from Bearer JWT for these endpoints, that change hasn't landed in the code yet — let me know if you'd like help implementing it.

---

## Configuration Profiles

Two Spring profiles are provided under `rest/src/main/resources/`, selected via `-P<profile>` at build time (Maven resource filtering sets `spring.profiles.active`).

| Aspect | `dev` (`application-dev.properties`) | `test` (`application-test.properties`) |
|---|---|---|
| Database | MySQL 8.4 (`com.mysql.cj.jdbc.Driver`), URL/user/password from `WELLDEV_*` env vars | H2 in-memory (`jdbc:h2:mem:testdb`), user `sa` / no password |
| Flyway | **Enabled** — runs migrations from `classpath:db.migration`, `baseline-on-migrate=true` | **Disabled** |
| Connection pool | HikariCP fully tuned (max-pool-size 20, min-idle 10, prepared-statement caching, `TRANSACTION_READ_COMMITTED`) | No pool tuning — H2 default |
| SQL logging | `spring.jpa.show-sql=false`, Hibernate SQL log level `warn` | `spring.jpa.show-sql=true`, Hibernate SQL log level `info` |
| Schema | Managed by Flyway | `spring.jpa.generate-ddl=true` (Hibernate generates the schema) |
| H2 Console | N/A | Enabled at `/h2-console` |
| Requires | MySQL + Keycloak (via `docker-compose`) | Nothing external — fully self-contained, ideal for CI/unit-style runs |

Build/run with a given profile:

```bash
mvn clean package -Pdev    # MySQL-backed
mvn clean package -Ptest   # H2 in-memory, no external DB needed
```

---

## API Endpoints

All secured endpoints require a `Bearer` JWT in the `Authorization` header. Requests are routed by Spring's built-in **API versioning** (`@RequestMapping(version = "...")`), matched against the `X-API-Version` header — `1.0` for `/api/v1/mono/**` controllers, `2.0` for the `v2` auth controller.

### `AuthenticationControllerV2` — `/api/v2/auth` (public, `X-API-Version: 2.0`)

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v2/auth/register` | Registers a new user in Keycloak + local DB (`UserRequest` body). Returns `TokenResponse`. |
| `POST` | `/api/v2/auth/login` | Authenticates via Keycloak (`AuthenticationRequest` body). Returns `TokenResponse` (access + refresh token). |

### `UserController` — `/api/v1/mono/user` (`X-API-Version: 1.0`)

| Method | Path | Required Authority | Description |
|---|---|---|---|
| `PUT` | `/api/v1/mono/user/update?username={username}` | `user:update` + must match `authentication.name` | Updates the authenticated user's profile (`UpdateUserRequest` body). |
| `DELETE` | `/api/v1/mono/user/delete/{username}` | `user:delete` + must match `authentication.name` | Deletes the authenticated user's account. |

### `TodoController` — `/api/v1/mono/todo` (`X-API-Version: 1.0`)

| Method | Path | Required Authority | Description |
|---|---|---|---|
| `POST` | `/api/v1/mono/todo/{username}` | `user:create` + must match `authentication.name` | Fetches todos from JSONPlaceholder for the given user and persists them. |
| `POST` | `/api/v1/mono/todo/save/{username}` | `user:create` + must match `authentication.name` | Saves a custom list of todos (`TodoRequest[]` body) for the given user. |
| `GET` | `/api/v1/mono/todo/all-todos/{username}` | `user:read` + must match `authentication.name` | Paginated list (`TodoUsernameResponse`) of todos for a specific user. Response is cached (`todoCache`). |

> **Note:** `TodoController.findAllTodos` (a Blaze Persistence `TodoView` projection guarded by `user:read`) currently has no `@GetMapping`/route annotation and is therefore not yet reachable over HTTP — it needs a mapping annotation (e.g. `@GetMapping("/all-todos")`) before it can serve the paginated "all todos" use case described by the `adminCache`/`todoCache` design.

### `AdminController` — `/api/v1/mono/admin` (`X-API-Version: 1.0`)

| Method | Path | Required Authority | Description |
|---|---|---|---|
| `GET` | `/api/v1/mono/admin/{id}` | `admin:read` | Retrieves a user by ID (`UserResponse` admin projection). Returns 404 if not found. |

### Pagination

Paginated endpoints accept `?pageNum=0&pageSize=10` query parameters. Responses use Spring Data's `Page<T>` serialised as a DTO (via `EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)`).

---

## OpenAPI Code Generation

The project uses the **OpenAPI Generator Maven Plugin** (`openapi-generator-maven-plugin 7.22.0`) against a central spec at `openapi/json-api-holder.yaml`. It runs two executions:

| Execution | Generator | Output |
|---|---|---|
| `Generate Controller and Dtos` | `spring` | DTO classes under `mu.server.service.dto` (suffix `Dto`) |
| `Generate RestClient APIs` | `java` (restclient library) | HTTP exchange interfaces under `mu.server.service.dto.api` |

Generated sources land in `target/generated-sources` and are automatically compiled with the rest of the project. This keeps API contracts in one place and prevents drift between the spec and implementation.

---

## Infrastructure

Start all services with:

```bash
cd infra && docker-compose up -d
```

| Service | Image | Port | Notes |
|---|---|---|---|
| MySQL | `mysql:8.4` | `3306` | Application DB — health-checked with `mysqladmin ping` |
| PostgreSQL | `postgres:17-alpine` | `5432` | Keycloak's backing DB — health-checked with `pg_isready` |
| Keycloak | `keycloak:26.6.1` | `7080` | Imports realm on startup; depends on Postgres being healthy |

All services are connected on a shared `observability` bridge network and have resource limits applied (`512M` / `1 CPU` for MySQL and Postgres; `1G` / `1 CPU` for Keycloak). Persistent volumes (`mysql-data`, `postgres-data`) survive container restarts.

### Flyway Migrations

Migrations live in `persistence/src/main/resources/db.migration/` and run automatically on startup.

| Version | Description |
|---|---|
| `V1_0` | Create initial tables: `_user`, `todo`, `post`, `comments` |
| `V1_1` | Alter `_user` table — add additional user fields |
| `V1_2` | Create `token` table |
| `V1_3` | Alter `_user` table — restructure columns |
| `V1_4` | Add auditing columns to `_user` (`created_date`, `last_modified_date`, `created_by`, `modified_by`) |
| `V1_5` | Create a view joining `_user` and `todo` |
| `V1_6` | Drop `token` table (tokens are now managed by Keycloak) |
| `V1_7` | Add `keycloak_id` column to `_user` |

---

## Performance Highlights

### Virtual Threads
`spring.threads.virtual.enabled: true` — all Tomcat request threads are Java 25 virtual threads, enabling high throughput without increasing OS thread count.

### Tomcat Tuning

| Setting | Value | Purpose |
|---|---|---|
| `min-spare` | 20 | Minimum threads kept alive |
| `max` | 200 | Maximum concurrent request threads |
| `connection-timeout` | 20 000 ms | Time to establish a connection |
| `keep-alive-timeout` | 15 000 ms | Idle connection timeout |
| `max-keep-alive-requests` | 100 | Requests per persistent connection |
| `accept-count` | 100 | Request queue depth when all threads are busy |

### HikariCP Connection Pool

| Setting | Value |
|---|---|
| `maximum-pool-size` | 20 |
| `minimum-idle` | 10 |
| `idle-timeout` | 300 000 ms |
| `max-lifetime` | 1 800 000 ms |
| `connection-timeout` | 30 000 ms |
| `leak-detection-threshold` | 2 000 ms |
| `transaction-isolation` | `TRANSACTION_READ_COMMITTED` |
| `cachePrepStmts` | `true` (cache size 500, limit 1024 chars) |

### Hibernate Batching

| Setting | Value |
|---|---|
| `jdbc.batch_size` | 50 |
| `jdbc.fetch_size` | 100 |
| `order_inserts` / `order_updates` | `true` |
| `default_batch_fetch_size` | 32 |

### Caffeine Cache

Five named caches with a 300-second TTL and a maximum of 10 entries each:

| Cache | Used By |
|---|---|
| `fingerprintCache` | Token fingerprint storage in `FingerprintFilter` |
| `userCache` | User lookups |
| `todoCache` | Todo list responses |
| `adminCache` | Admin projection responses |
| `jsonPlaceHolder` | External API responses from JSONPlaceholder |

### Other

| Feature | Detail |
|---|---|
| Gzip compression | Enabled for `application/json`, `text/plain`, `application/xml` ≥ 1 KB |
| Retry | `@Retryable` — 4 attempts, 2 s initial delay, ×2 backoff multiplier |
| Async | `ThreadPoolTaskExecutor` — core=3, max=10 |
| Scheduler | `ThreadPoolTaskScheduler` — 4 threads |
| Lazy init | `spring.main.lazy-initialization: true` for faster startup |

---

## Error Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) centralises error responses across all controllers. Every error response follows the same structure:

```json
{
  "statusCode": 404,
  "timestamp": "2026-06-25T10:00:00",
  "message": "...",
  "description": "..."
}
```

| Exception | HTTP Status |
|---|---|
| `DataIntegrityViolationException` | 500 Internal Server Error |
| `NoResourceFoundException` | 404 Not Found |
| `UsernameExistException` | 400 Bad Request |

---

## CI/CD Pipeline

The project uses a **modular GitHub Actions pipeline** — a single orchestrator workflow that calls out to focused, reusable workflows for each concern. This keeps individual jobs small, parallelizable, and independently testable, instead of one monolithic workflow file.

```
                        ┌────────────────────┐
                        │   ci.yml (Main CI  │
                        │     Orchestrator)  │
                        └─────────┬──────────┘
                                  │
                 ┌────────────────┼────────────────┬──────────────┐
                 ▼                ▼                ▼              ▼
            build.yml      unit-tests.yml  integration-tests.yml  security.yml
          (compile & pkg)   (mvn test)        (mvn verify)     (CodeQL + Gitleaks)
                 │                │                │                │
                 └────────────────┴────────┬───────┴────────────────┘
                                            ▼
                                     tag.yml (PR only)
                                            │
                                            ▼
                                      build-gate
                                (aggregates all results)
```

### Trigger Strategy

| Event | Scope |
|---|---|
| `push` | Any branch except `dependabot/**` |
| `pull_request` | Targeting `main` |

Concurrency is scoped per-workflow-per-ref (`cancel-in-progress: true`), so pushing new commits to the same branch/PR automatically cancels any in-flight run for that ref.

### Workflow Modules

| Workflow | Trigger | Responsibility |
|---|---|---|
| `ci.yml` | `push` / `pull_request` | Orchestrator — wires up all reusable workflows and computes the final gate |
| `build.yml` | `workflow_call` | Rejects tracked `.env` files, compiles & packages with `mvn verify -DskipTests`, submits the Maven dependency graph on `push` |
| `unit-tests.yml` | `workflow_call` | Runs `mvn test` |
| `integration-tests.yml` | `workflow_call` | Runs `mvn verify -Dsurefire.skip=true` against MySQL/Postgres/Keycloak-backed integration tests |
| `security.yml` | `workflow_call` | Runs CodeQL (Java/Kotlin) static analysis and Gitleaks secret scanning in parallel |
| `tag.yml` | `workflow_call` (PR → `main` only) | Tags the PR build (`pr-<number>-run-<run_number>`) and prunes older tags for the same PR, keeping the latest 4 |

All Java setup (JDK 25, Maven caching, `MAVEN_OPTS`) is centralized in the composite action `.github/setup-java-env`, so every workflow module configures the toolchain identically.

### `build-gate` — Single Source of Truth

`build-gate` is the only job branch protection should require. It waits on `build`, `unit-tests`, `integration-tests`, `security`, and `tag`, then fails only if any of them explicitly report `failure` — a `skipped` result (e.g. `tag` skipping outside of PR-to-`main` context) is treated as passing. This avoids the classic GitHub Actions pitfall where a conditionally-skipped required check permanently blocks merging.

---

## Getting Started

### Prerequisites

- Java 25
- Maven 3.9.9
- Docker & Docker Compose

### 1. Start Infrastructure First (required for the `dev` profile)

The application (in `dev` profile) depends on MySQL and Keycloak (backed by PostgreSQL) already running before it starts — Flyway migrations run on boot and will fail if the database isn't reachable.

```bash
cd infra

# Copy the example env file and fill in real values (DB credentials, Keycloak admin, hostname, etc.)
cp .env.example .env

# Start MySQL, PostgreSQL, and Keycloak in the background
docker-compose up -d

cd ..
```

Required variables in `infra/.env` (see `.env.example`):

| Variable | Used by |
|---|---|
| `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD` | `mysql` service — the application's database |
| `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB` | `postgres` service — Keycloak's backing store |
| `KEYCLOAK_HOSTNAME`, `KEYCLOAK_ADMIN`, `KEYCLOAK_ADMIN_PASSWORD` | `keycloak` service |

Startup order is enforced by the compose file: `keycloak` waits for `postgres` to report healthy (`depends_on: condition: service_healthy`) before starting; `mysql` starts independently. Check everything is healthy with:

```bash
docker-compose ps
```

- MySQL → `localhost:3306`
- PostgreSQL → `localhost:5432`
- Keycloak → `localhost:7080` (realm auto-imported from `infra/keycloak/`)

### 2. Build and Run the Application

```bash
# dev profile (MySQL + Keycloak — requires step 1 above to be running)
mvn clean package -Pdev
java -jar rest/target/rest-1.0-SNAPSHOT.jar

# — OR —

# test profile (H2 in-memory — no docker-compose / external DB required)
mvn clean package -Ptest
java -jar rest/target/rest-1.0-SNAPSHOT.jar
```

The `dev` profile is active by default and requires the infrastructure from step 1 to be up. The `test` profile swaps the datasource for H2 in-memory, disables Flyway, and needs **no external services** — useful for quick local runs or CI without Docker.

---

## API Docs

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON spec | http://localhost:8080/v3/api-docs |
| H2 Console *(test profile only)* | http://localhost:8080/h2-console · user `sa` / no password |
