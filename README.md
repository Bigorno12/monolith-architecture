# 🏗️ Monolith Architecture

> Production-grade **modular monolith** — Spring Boot 4 · Kotlin 2.4 · Java 25

---

## What is a Monolith Architecture?

A **monolith** is a single deployable unit where all application concerns — UI, business logic, and data access — live in the same codebase and are built and deployed together.

```
┌─────────────────────────────────┐
│         Single Process          │
│  ┌──────┐ ┌─────────┐ ┌──────┐ │
│  │  UI  │ │Business │ │  DB  │ │
│  │Layer │ │ Logic   │ │Layer │ │
│  └──────┘ └─────────┘ └──────┘ │
└─────────────────────────────────┘
         One deployable JAR
```

**Advantages:**
- Simple to develop, test, and deploy
- No network overhead between layers (in-process calls)
- Easy transactions across the whole domain
- Less operational complexity vs microservices

**Disadvantages:**
- Full redeploy required for any change
- Can become hard to maintain as the codebase grows
- Scaling requires replicating the entire app, not just hot spots

### Modular Monolith (this project)

A **modular monolith** keeps the single-deployment simplicity but enforces **clear internal boundaries** via Maven modules. Each module has its own package scope and can only access what it explicitly depends on — the same separation you'd get from microservices, without the distributed systems complexity.

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

---

## Architecture

Three Maven modules with a strict one-directional dependency flow:

```
rest  ──▶  service  ──▶  persistence
 │              │               │
 ▼              ▼               ▼
Keycloak   JSONPlaceholder   MySQL / H2
```

| Module | Responsibility |
|---|---|
| `persistence` | JPA entities, repositories, Flyway migrations |
| `service` | Business logic, DTOs, MapStruct mappers, HTTP clients |
| `rest` | Controllers, security filters, Spring configs, entry point |

---

## Tech Stack

| Category | Technology | Version |
|---|---|---|
| Runtime | Java | 25 |
| Language | Kotlin + Java | Kotlin 2.4.0 |
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
| Object Mapping | MapStruct | 1.6.3 |
| Boilerplate | Lombok | Boot-managed |
| API Docs | SpringDoc / Swagger UI | 3.0.3 |
| HTTP Client | Spring RestClient `@HttpExchange` | Boot-managed |
| Keycloak Admin | Admin Client + RESTEasy | 26.0.9 / 7.0.2.Final |
| Concurrency | Java Virtual Threads | Java 25 |
| Infra | Docker Compose | — |

---

## Domain Model

```
User ──< Todo
User ──< Post ──< Comment
```

**User** — `id, firstname, lastname, age, gender, username, email` *(AES-256 encrypted)*, `keycloakId, role` + audit fields  
**Todo** — `id, title, completed, user_id (FK)`  
**Post** — `id, title, body, user_id (FK)`  
**Comment** — `id, email, name, body, post_id (FK)`

All entities extend `Auditable` (`createdDate`, `lastModifiedDate`, `createdBy`, `modifiedBy`).

---

## Security

### Filter Chain (per request)
```
BearerTokenAuthenticationFilter  ←  validates Keycloak JWT
       ↓
FingerprintFilter                 ←  anti token-hijacking (IP + User-Agent)
       ↓
RateLimitFilter                   ←  Bucket4j: 100 req/min → HTTP 429
       ↓
Controller
```

### Two SecurityFilterChains

| Chain | Matcher | Mode |
|---|---|---|
| 1 | `/api/v1/mono/**` | Stateless JWT resource server |
| 2 | All other paths | OAuth2 OIDC login / logout |

### Roles & Permissions

| Role | Permissions |
|---|---|
| `USER` | `user:create/read/update/delete` · `admin:read` |
| `ADMIN` | `admin:create/read/update/delete` |

---

## API Endpoints

| Method | Path | Auth |
|---|---|---|
| `POST` | `/api/v2/auth/register` | Public |
| `POST` | `/api/v2/auth/login` | Public |
| `PUT` | `/api/v1/mono/user/update` | `user:update` |
| `POST` | `/api/v1/mono/todo/{username}` | `user:create` |
| `POST` | `/api/v1/mono/todo/save/{username}` | `user:create` |
| `GET` | `/api/v1/mono/todo/all-todos/{username}` | `user:read` |
| `GET` | `/api/v1/mono/todo/all-todos` | `user:read` |
| `GET` | `/api/v1/mono/admin/{id}` | `admin:read` |

All requests pass `X-API-Version: 1.0` header.

---

## Infrastructure

```bash
cd infra && docker-compose up -d
```

| Service | Image | Port |
|---|---|---|
| MySQL | `mysql:8.4` | 3306 |
| PostgreSQL | `postgres:17-alpine` | 5432 |
| Keycloak | `keycloak:26.6.1` | 7080 |

**Flyway migrations:** `V1_0` (create tables) → `V1_7` (add `keycloak_id`)

---

## Performance Highlights

| Feature | Config |
|---|---|
| Virtual Threads | `spring.threads.virtual.enabled: true` |
| HikariCP | max=20, batch inserts/updates |
| Caffeine Cache | 5 caches · 300s TTL · `fingerprintCache, userCache, todoCache, adminCache, jsonPlaceHolder` |
| Gzip | JSON/plain ≥ 1 KB |
| Hibernate batching | `batch_size=50`, `fetch_size=100` |
| Retry | `@Retryable` 4× · 2s delay · ×2 multiplier |
| Async | `ThreadPoolTaskExecutor` core=3, max=10 |
| Scheduler | `ThreadPoolTaskScheduler` 4 threads |

---

## Getting Started

### Prerequisites
Java 25 · Maven 3.9.9 · Docker

### Environment Variables
```bash
WELLDEV_URL=jdbc:mysql://localhost:3306/your_db
WELLDEV_USERNAME=your_user
WELLDEV_PASSWORD=your_password

WELLDEV.KEYCLOAK.SERVER.URL=http://localhost:7080
WELLDEV.KEYCLOAK.REALM=monolith-keycloak
WELLDEV.KEYCLOAK.CLIENT-ID=your-client-id
WELLDEV.KEYCLOAK.CLIENT-SECRET=your-client-secret
WELLDEV.KEYCLOAK.ISSUER-URI=http://localhost:7080/realms/monolith-keycloak

WELLDEV.AES.SECRET.KEY=your-32-char-key
```

### Run

```bash
# Dev (MySQL) — default
mvn clean package -Pdev
java -jar rest/target/rest-1.0-SNAPSHOT.jar

# Test (H2)
mvn clean package -Ptest
java -jar rest/target/rest-1.0-SNAPSHOT.jar
```

---

## API Docs

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI spec | http://localhost:8080/v3/api-docs |
| H2 Console *(test)* | http://localhost:8080/h2-console · `sa` / *(empty)* |
