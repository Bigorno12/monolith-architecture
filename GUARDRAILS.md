# Monolith Guardrails

Automated checks against accidental drift. They run **locally** in `.githook/` and Claude Code
hooks, in the **build** (`mvn clean test`, Spotless and Enforcer at `validate`), and
**remotely** in `.github/workflows/ci.yml`.

> **Principle: no hook without a CI backstop.** Hooks are advisory. A developer can
> `--no-verify`, or never install them, and an agent can be configured around them. CI is
> mandatory. So every local check needs a CI job running the *same* check. The only
> deliberate exception is the `secret.env`/`local.env`/`config.env` value scan, because its
> input files are gitignored and exist only on a developer's machine.
>
> Here the principle is **mostly met**: `ci.yml` delegates to the reusable
> `Bigorno12/ci-cd-templates` pipeline (build → lint → tests → CodeQL/Gitleaks), pinned to a
> commit SHA. What that pipeline runs is defined in the template repo, not here. When a CI
> column below says ✓, it means "the template's build/test/Gitleaks stage covers it".

## In place

### Architecture

| Guardrail | What it protects | How | CI |
|---|---|---|---|
| `ArchitectureTest` (ArchUnit, `rest/src/test/java`) | Layering `rest → service → persistence` | Layered rules over `controller`/`service`/`persistence`/`config`/`advice`/`filter`; `@Service` under `..service..`, `@Entity`/repositories under `..persistence..`, controllers in `controller` | ✓ |
| `ArchitectureTest`: `@Transactional` rule | Transaction boundaries | Every public `*ServiceImpl` method is `@Transactional`, except `fallback*` and `authenticate` | ✓ |
| `ArchitectureTest`: `enumeration` layer | The one persistence slice upper layers may import | Every persistence enum lives in `..persistence.enumeration..`, and `enumeration` depends on nothing else in `persistence` | ✓ |
| `ArchitectureTest`: no field injection | Constructor injection everywhere | `NO_CLASSES_SHOULD_USE_FIELD_INJECTION` | ✓ |
| Maven module graph | `persistence` can't see `service`/`rest` | `persistence/pom.xml` has no dependency on the upper modules, so the import doesn't compile | ✓ |
| Flyway checksums | Applied migrations stay immutable | Flyway refuses to start when an applied migration's checksum changed | ✗ (only at app start-up with `-Pdev`; no test runs Flyway) |
| `RateLimitFilterTest` | `Tier` limits, per-caller buckets, `/actuator/**` exempt | Unit tests of `RateLimitFilter` | ✓ |

### Code quality

| Guardrail | What it protects | How | CI |
|---|---|---|---|
| Spotless (Java, ktlint for Kotlin, sortPom) | Formatting and import order of `.java`, `src/{main,test}/java/**/*.kt`, `pom.xml` | `spotless:check` binds to `validate`, so every build fails on an unformatted file. `pre-commit` runs `spotless:apply` and re-stages staged `.java`/`.kt` | ✓ |
| Maven Enforcer | Dependency drift, toolchain | `dependencyConvergence`, `requireUpperBoundDeps`, `requirePluginVersions`, Java ≥ 25, Maven ≥ 3.9.9 | ✓ |
| `post-java-kotlin-edit.sh` (Claude hook) | Source root, FQCNs | After every Write/Edit of a `.java`/`.kt`, flags a file under `src/{main,test}/kotlin` (compiled by nothing) and fully-qualified class names in code | n/a (agent-only) |
| Rules loaded by path | Kotlin and `pom.xml` conventions | `.claude/rules/kotlin-rule.md`, `.claude/rules/pom-rule.md` | n/a (agent-only) |
| `pre-read-token-guard.sh` (Claude hook) | Context window | Denies whole-file `Read`/`cat` over `TOKEN_GUARD_MAX_LINES` (350) | n/a (agent-only) |

### Secrets and dependencies

| Guardrail | What it protects | How | CI |
|---|---|---|---|
| gitleaks | Secrets in commits | `pre-commit` runs `gitleaks protect --staged` and blocks on a hit (skipped with a warning if gitleaks isn't installed) | ✓ (Gitleaks stage) |
| Local env-value scan | Values from `secret.env`/`local.env`/`config.env` leaking into code | `pre-commit` compares staged additions with each value of 8+ characters. ⚠️ **Currently inert**: it reads the files from the repo root, but they live in `infra/` (see Gaps) | — (deliberately local) |
| `Read(./**/*.env)` deny | Agents reading credentials | `.claude/settings.json` `permissions.deny`. It covers the Read tool only; CLAUDE.md's "never open an env file" covers the shell | n/a |
| `.aiignore` | Non-Claude AI tools reading credentials or generated code | Mirrors the secret patterns of `.gitignore` | n/a |
| CodeQL | Code-level vulnerabilities | Reusable pipeline, SARIF upload (`security-events: write`) | ✓ |
| `.trivyignore` | Explicitly accepted image CVEs | Each entry names the CVE and what it is. Read by Trivy, which runs in the template repo, not here | ? (defined in the template) |
| Cosign keyless signing | Image provenance | CI signs the GHCR image; `signer-identity-regexp` pins the template repo | ✓ |
| Dependabot | Stale dependencies, drive-by upgrades | `maven`, `docker-compose`, `github-actions`, weekly, cooldowns (7d / 10d major / 3d patch), grouped PRs | ✓ (it *is* CI) |

### Process

| Guardrail | What it protects | How | CI |
|---|---|---|---|
| `pre-push` | Pushing a red build | Runs `mvn clean test -B` when `.java`/`.kt`/`pom.xml`/`.properties`/`.yml`/`.yaml` changed | ✓ (same tests in CI) |
| `--no-verify` deny | Agents skipping the hooks | `.claude/settings.json` denies `git commit --no-verify` / `git push --no-verify` | n/a |
| `core.hooksPath` self-install | Hooks silently disabled | `exec-maven-plugin` sets `core.hooksPath=.githook` on every build, and a SessionStart hook restores it | n/a |
| `ask` on sensitive files | Agents loosening a guardrail, or touching the CI-owned tag | `.claude/settings.json` asks before editing itself and `infra/k8s/manifest/api.yaml` | n/a |
| `CODEOWNERS` | Unreviewed merges | `*` → `@Bigorno12` on every PR | ✓ (if branch protection requires it) |
| `/multi-review` + REVIEW.md | Reuse/simplification/efficiency regressions | Three `reviewer-*` agents in parallel, findings filtered by REVIEW.md's "concrete failing scenario" rule | n/a (agent-only) |
| `detect-concurrent-sessions.sh` | Two Claude sessions editing one checkout | SessionStart warning, suggests a worktree | n/a |
| `scheduled-maintenance.yml` | Flaky red runs, cache bloat | Nightly 02:00 UTC re-run of up to 5 failed `ci.yml` runs (attempt 1 only), prunes non-`main` caches > 5 days | ✓ |
| Pod Security Admission | Privileged/host-namespace pods in `monolith` | `namespace.yaml` enforces `baseline`, warns + audits `restricted` | ✓ (applied by Argo CD) |
| NetworkPolicy | Lateral movement inside the namespace | `networkpolicy.yaml`: default-deny ingress + one allow per connection. Enforced only with a policy-capable CNI (Calico in `minikube-cluster.sh`) | ✓ (applied by Argo CD) |
| GitOps self-heal | Live drift in the cluster | Argo CD syncs `infra/k8s/manifest` from `main` with prune + self-heal | ✓ |

## Gaps and next steps

In priority order:

1. **Fix the env-value scan path.** `.githook/pre-commit` sets
   `ENV_FILES=("$ROOT/secret.env" …)`, but `infra/` holds the files. Use `$ROOT/infra/*.env`.
   Until then the only local secret check is gitleaks, and only if it's installed.
2. **A context-booting test.** Nothing proves `SecurityConfig`, the two filter chains, the
   `FilterRegistrationBean`s or the Flyway migrations at build time.
   `TestContainerDBConfiguration` exists for exactly this and is used by no test.
3. **Widen the `ask` list.** Editing `ArchitectureTest.java`, `.githook/**`,
   `.github/workflows/**` or an applied `db.migration/*.sql` loosens a guardrail with no prompt.
4. **Structural lint for prose rules.** "A convention nobody checks is a suggestion."
   Candidates for ArchUnit or [ast-grep](https://ast-grep.github.io/): every `@Cacheable` name
   is in `spring.cache.cache-names`; every `@Component` filter has a disabled
   `FilterRegistrationBean`; every `@CircuitBreaker` has a matching `fallback*`.
5. **Diagram drift.** `docs/*.puml` is hand-maintained; `ArchitectureTest` could emit the
   `packages.puml` it asserts (see ARCHITECTURE.md → Making these generated).
6. **`AGENTS.md` symlink check.** Nothing warns if a tool replaces the link with a copy.

## Considered, not scheduled

- **Coverage floor**: JaCoCo, ideally on new code only.
- **`.editorconfig`** for the files Spotless doesn't touch (YAML, SQL, Markdown).
- **`.DS_Store` in `.gitignore`**: it's in `.aiignore` but not `.gitignore`, so it shows up untracked.
