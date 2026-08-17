# Project Memory

Coding agents auto-load this file in any new conversation in this folder.
It's the most important file in any repo, pushed on git, added to on any AI failure/slop, carefully 👱🏻‍♂️-curated every retrospective.
CLAUDE.md is symlinked to [standard](https://agents.md) AGENTS.md, as GitHub Copilot prefers it.
Copilot: use this file over your proprietary .github/copilot-instructions.md

## Project Overview

Production-grade **modular monolith** — a Keycloak-secured REST API over MySQL, deployed to Kubernetes via GitOps. Java 25 + Kotlin 2.4 on Spring Boot 4, one Maven reactor, one bootable jar.

**Structure:** three Maven modules, strictly one-way `rest → service → persistence`
- `rest/` — HTTP layer: controllers, filters, `@RestControllerAdvice`, all `@Configuration`. **The only bootable module** (`mu.server.rest.Application`)
- `service/` — business logic, MapStruct mappers, DTOs, Resilience4j circuit breakers, Caffeine caching
- `persistence/` — JPA entities, Spring Data + Blaze-Persistence repositories, Flyway migrations
- `infra/` — docker-compose, Keycloak realm, k8s manifests, `.env` templates
- `openapi/json-api-holder.yaml` — the API contract; **source** for generated DTOs/clients

**Companion docs — read the one that matches the task:**

| File | When it applies |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) + [`docs/*.puml`](docs/) | Domain model, ER, layering, register sequence, C4 context. Hand-maintained: update the `.puml` in the same commit as the code it describes. |
| [`.claude/rules/pom-rule.md`](.claude/rules/pom-rule.md) | **Before touching any `pom.xml`.** Where versions may live, and the enforcer/Spotless gates that reject the alternatives. |
| [`.claude/boris-CLAUDE.md`](.claude/boris-CLAUDE.md) | How to work: planning, subagents, verification-before-done, autonomous CI repair. |
| [`.claude/agents/reviewer-*.md`](.claude/agents/) | Three single-focus reviewers (reuse / simplification / efficiency), run in parallel as one pass. |
| [`.claude/settings.json`](.claude/settings.json) | Shared hooks + permission policy. Personal allowlists go in `settings.local.json` (gitignored). |

## Common Commands

### Local infrastructure (infra/)
```sh
cp .env.example secret.env                          # fill MYSQL_*, POSTGRES_*, KEYCLOAK_*, WELLDEV.*
docker-compose --env-file secret.env up -d          # MySQL :3306, Postgres :5432, Keycloak :7080
docker-compose --env-file secret.env down -v        # wipe volumes (fixes stale MySQL users)
```

### Build & run (repo root)
```sh
mvn clean package -Pdev                             # MySQL profile (default)
mvn clean package -Ptest                            # H2 in-memory, no Docker needed
java -jar rest/target/rest-1.0-SNAPSHOT.jar         # :8080

mvn clean test                                      # full test run (what pre-push runs)
mvn test -pl rest -Dtest=ArchitectureTest#layer_checks_test   # a single test
mvn install -pl service -am                         # one module + its dependencies
mvn spotless:apply                                  # format Java + Kotlin + pom.xml
mvn clean package spring-boot:build-image -Pdev -pl rest -am  # Paketo image, no Dockerfile
```

### Kubernetes (infra/k8s/)
```sh
kind/kind-cluster.sh create                         # local cluster
./bootstrap-gitops.sh                               # seeds monolith-secrets, installs Argo CD
./check-read.sh                                     # pod health + logs for failures
kind/kind-cluster.sh destroy
```

## Architecture

### Layered Structure
1. Controllers (`rest/controller/`) — thin, `ResponseEntity`-returning, `@PreAuthorize` per method
2. Services (`service/`, interface) → (`service/impl/`, `*ServiceImpl`) — transactions, caching, circuit breakers
3. Mappers (`service/mapper/`) — MapStruct entity↔DTO, `componentModel = "spring"`
4. Repositories (`persistence/repository/`) — Spring Data JPA; Blaze entity views for paginated projections
5. Entities (`persistence/entity/`) — all extend `Auditable`

**Data flow:** Request → Controller → Service (`@Transactional`) → Repository → Entity
**Response:** ← Controller ← Mapper (Entity→DTO) ← Service

### Guardrails — `ArchitectureTest` (rest/src/test) is the main test suite
ArchUnit rules that fail the build; read them before restructuring packages:
- `controller` accessed by nobody; `service` only by controller/config/advice/filter; `persistence` only by service/config/controller
- **Every public `*ServiceImpl` method must be `@Transactional`**, except names matching `fallback*` or `authenticate`
- `@Service`/`*Service` types must live under `..service..`; `@Entity` types under `..persistence..`
- `persistence` may not depend on `service` or `controller`

### Generated Code — never edit, edit the source
- `openapi/json-api-holder.yaml` → `openapi-generator-maven-plugin`, two executions per module:
  `service` emits models (`mu.server.service.dto`, `Dto` suffix), `rest` emits API interfaces + a RestClient
- MapStruct + Lombok implementations → `target/generated-sources/` (regenerate with `mvn clean install`)
- Hand-written DTOs (Java records / Kotlin data classes) live beside the generated ones in `service/.../dto/`

### Java + Kotlin coexist in `src/main/java`
`kotlin-maven-plugin` compiles `src/main/java`, and Spotless only includes `src/main/java/**/*.kt`.
**Put new Kotlin files in `src/main/java`, not `src/main/kotlin`** — the latter is compiled by nothing and formatted by nothing.

### Cross-cutting
- **Caching** — `CaffeineConfig` names caches explicitly. A new cache name must be added *both* there and to `spring.cache.cache-names`, else `@Cacheable` silently no-ops. `fingerprintCache` is a separate custom cache (30 min) used by `FingerprintFilter` to reject tokens replayed from another client fingerprint.
- **Resilience** — `@CircuitBreaker(name = "userService"|"keycloakService")`; instances configured in `application.properties`; each `fallback*` method mirrors the guarded signature plus a trailing `Throwable`.
- **Outbound HTTP** — declarative `@HttpExchange` interfaces (`JsonPlaceHolderService`) registered via `@ImportHttpServices` in `RestClientConfig`, with `@Retryable` + `@EnableResilientMethods`.
- **API versioning** — Spring MVC's native `version` attribute on `@RequestMapping`/`@GetMapping`, resolved from the `X-API-Version` header (`spring.mvc.apiversion.use.header`). Path segment and declared version are kept in sync by convention.

## Configuration & Profiles

**The Maven profile bakes the Spring profile into the jar.** `spring.profiles.active=@spring.profiles.active@` is resource-filtered at package time: `-Pdev` → MySQL + Flyway on, `-Ptest` → H2 + Flyway off. Switching profiles requires a rebuild.

All secrets come from env vars matching `infra/secret.env` — `WELLDEV_URL`/`WELLDEV_USERNAME`/`WELLDEV_PASSWORD` (datasource), `WELLDEV.KEYCLOAK.*` + `WELLDEV.RESOURCESERVER.*` (OIDC), `WELLDEV.AES.SECRET.KEY` (column encryption). Any missing value fails startup.

🚫 **Never open an env file.** `secret.env`, `local.env`, `config.env`, and any `.env*` hold live credentials — do not `Read`, `cat`, `grep`, `head`, `tail`, or otherwise print them, and never echo a value read from one into a file, a command, or the chat. `infra/.env.example` is the only exception: it is committed, all values are blank, and it is the correct place to look up *which* variables exist. If a task seems to need a real value, say which variable you need and let the human supply or export it.

This is enforced, not just requested: `.claude/settings.json` denies `Read(./**/*.env)` (deliberately *not* `.env.*`, so `.env.example` stays readable). The deny covers the Read tool — a shell `cat`/`grep` can still reach the file, which is exactly why the rule above exists.

Build-time gates that fail before code compiles:
- **Spotless** `check` binds to `validate` — unformatted Java/Kotlin/`pom.xml` fails *every* build. Run `spotless:apply` first.
- **Enforcer** applies `dependencyConvergence`, `requireUpperBoundDeps`, `requirePluginVersions` — pin new versions in the root `dependencyManagement`. Java ≥ 25 and Maven ≥ 3.9.9 required.
- **Git hooks install themselves** on any build (`exec-maven-plugin` sets `core.hooksPath=.githook`). `pre-commit` = gitleaks + a scan for `secret.env`/`local.env`/`config.env` values in staged additions + `spotless:apply` with re-staging. `pre-push` = `mvn clean test` when `.java`/`pom.xml`/`.properties`/`.yaml` changed.

## Database
- **Dev:** MySQL 9.x via docker-compose (`-Pdev`)
- **Tests:** H2 in-memory (`-Ptest`, `/h2-console`, user `sa`, no password), or Testcontainers MySQL + Postgres + Keycloak via `TestContainerDBConfiguration`
- **Flyway** migrations live in `persistence/src/main/resources/db.migration` — **a dot, not a slash** (`spring.flyway.locations=classpath:db.migration`). Add `V1_N__*.sql`, never edit an applied one. Disabled under `-Ptest`.
- `ddl-auto` is unset; `spring.jpa.open-in-view=false`, Hikari `auto-commit=false` — lazy loading outside a transaction will blow up
- Blaze-Persistence entity views (`repository/blaze/`, `BlazeConfig`) do paginated projections instead of JPA fetch joins; plain repositories extend `GenericRepository<T>`

## Security
- Keycloak-backed. **Two filter chains** in `SecurityConfig`:
  - `@Order(1)` `/api/v1/mono/**` — stateless JWT resource server, CSRF off, `FingerprintFilter` → `RateLimitFilter` after `BearerTokenAuthenticationFilter`
  - `@Order(2)` everything else — OAuth2 login client, cookie CSRF, OIDC-initiated logout
- Authorities are the `Permission` enum values **from the persistence module** (`user:read`, `admin:create`, …), mapped from Keycloak claims by `AuthoritiesConverter`/`KeycloakAuthenticationConverter`. The same strings are hardcoded in `@PreAuthorize` — **changing `Permission` touches persistence, `SecurityConfig`, and every controller**.
- Ownership checks use `#username == authentication.name` in `@PreAuthorize`
- `Role.USER` → `{user:create, user:update, user:delete, admin:read}`; `Role.ADMIN` → all `admin:*`
- `User.email` is encrypted at rest via `EncryptionConverter`/`AESConverter`

## API Endpoints
Base: http://localhost:8080 · Swagger UI: `/swagger-ui.html` · spec: `/v3/api-docs`
Send `X-API-Version` matching the endpoint's declared version.

| Endpoint | Auth |
|---|---|
| `POST /api/v2/auth/register`, `POST /api/v2/auth/login` | public (v2.0) |
| `POST /api/v1/auth/logout` | session (OIDC logout) |
| `GET /api/v1/mono/admin/{id}` | `admin:read` |
| `PUT /api/v1/mono/user/update?username=` | `user:update` + self |
| `DELETE /api/v1/mono/user/delete/{username}` | `user:delete` + self |
| `GET /api/v1/mono/user/view-profile/{username}` | `user:read` + self |
| `POST /api/v1/mono/todo/{username}` (import from JSONPlaceholder) | `user:create` + self |
| `POST /api/v1/mono/todo/save/{username}` | `user:create` + self |
| `GET /api/v1/mono/todo/all-todos/{username}` | `user:read` + self |

⚠️ `TodoController.findAllTodos` has `@PreAuthorize` but **no `@GetMapping`** — it is not routed. Add the mapping (and a path) before assuming it works.

## Domain Model
All entities extend `Auditable` (`createdDate`/`lastModifiedDate`/`createdBy`/`modifiedBy`, filled by `AuditorAwareImpl` from the security context).
- **User** 1→N **Todo** (`todo.user_id`) — table is `_user`
- **User** 1→N **Post** 1→N **Comment** (table `comments`)
- **User** carries `keycloakId` (unique, links to the Keycloak account), `role: Role`, `gender: Gender`, encrypted `email`
- **Role** → `Set<Permission>`; both enums live in `persistence/enumeration/`
- Deleting a user must delete both the row *and* the Keycloak account — see `UserServiceImpl.deleteUser`

## Deployment
- CI (`.github/workflows/ci.yml`) delegates the whole pipeline to the reusable `Bigorno12/ci-cd-templates` workflow: build → lint → tests → CodeQL/Gitleaks → image published to GHCR → GitOps bump.
- The pipeline **writes the image tag into `infra/k8s/manifest/api.yaml` and commits it** (`chore(gitops): update image tag …`). Don't hand-edit that tag.
- Argo CD (`infra/k8s/argo-app.yaml`) auto-syncs `infra/k8s/manifest` from `main` with prune + self-heal.
- `api.yaml` is a blue/green pair of Deployments behind one Service selected by the `color` label.
- `auto-release.yml` tags + releases every green merge to `main` (patch bump, keeps latest 10).
- ⚠️ `README.md` still documents `infra/k8s/deploy.sh`; it no longer exists — the Argo bootstrap replaced it.

## Development Notes

### Code Style
- Constructor injection everywhere: Kotlin primary constructors, Java `@RequiredArgsConstructor`
- Lombok only: `@Slf4j`, `@RequiredArgsConstructor`, `@Builder`, `@Getter`/`@Setter`. Setters are chained, accessors are not fluent (`lombok.config`)
- Kotlin logs via `private val LOG = LoggerFactory.getLogger(X::class.java)` in a `companion object`
- DTOs are Java records or Kotlin data classes; `Result<T>` (`ok`/`failure`) is the convention for "may not exist" service returns
- Import order is enforced: Java `,javax,java,\#` (statics last), Kotlin `*,java.**,javax.**,kotlin.**,^`. `mvn spotless:apply` handles it
- Service interface in `mu.server.service`, implementation in `mu.server.service.impl` named `*ServiceImpl`
- MapStruct mappers: `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)`
- New exceptions go in `service/exception/` **and** get a handler in `GlobalExceptionHandler` returning `ErrorMessage`
- `@Validated` on `@RequestBody` where input needs checking

## Task Modifiers
- 🚫 **Never read `.env` files** — `secret.env`, `local.env`, `config.env`, `.env*` are off-limits to every tool (Read, `cat`, `grep`, editor). Read `infra/.env.example` instead for the variable names; ask the human for the values. Never copy a secret into code, a commit, a log line, or the chat.
- Run `mvn spotless:apply` before committing — otherwise the next build fails at `validate`
- Always run `mvn test` after refactoring; ArchUnit guards the module boundaries and will catch a misplaced class before review does
- Never edit `target/generated-sources/` — change `openapi/json-api-holder.yaml` or the MapStruct interface
- Never edit an applied Flyway migration; add a new `V1_N__*.sql`
- Never hand-edit the image tag in `infra/k8s/manifest/api.yaml` — CI owns it
- Don't put real values in `.env.example`; `secret.env`/`local.env`/`config.env` are gitignored and the pre-commit hook blocks their values from leaking into other files
- Keep comments concise, prefer explanatory names; don't leave tombstone comments when deleting or moving code
- Keep explanations concise; challenge ambiguous prompts rather than guessing
