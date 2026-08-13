---
description: Run this project's local quality gate (format → build → test) and get it green
---

Run the same gate that `.githook/pre-push` and CI run, from the repo root, and get it green.

1. **`mvn spotless:apply` first, always.** Spotless `check` is bound to the `validate`
   phase, so one unformatted file fails every later step for a reason that has nothing
   to do with the code you changed.
2. **`mvn clean test`** — the whole reactor (`persistence` → `service` → `rest`).
   `ArchitectureTest` is the suite that matters here: it fails on layer violations, on a
   public `*ServiceImpl` method missing `@Transactional` (unless named `fallback*` or
   `authenticate`), and on `@Service`/`@Entity` types living in the wrong module.
3. **A failure at `initialize` is a dependency problem, not a code problem.** The
   enforcer's `dependencyConvergence` / `requireUpperBoundDeps` / `requirePluginVersions`
   want the version pinned in the **root** `pom.xml` `dependencyManagement` — not in a
   module pom, which only adds a second version to converge.

Fix what is mechanical — formatting, a misplaced class, a missing `@Transactional`, an
unpinned version — and re-run until green.

Stop and ask when a fix is a design decision rather than a repair. A failing ArchUnit rule
usually means the design moved, not that the test is wrong: don't relax the rule to make it
pass.

Notes:
- `-Ptest` (H2, Flyway off) needs no Docker; `-Pdev` (MySQL) needs `infra/docker-compose.yaml` up.
- Never read `secret.env` / `local.env` / `config.env` to make something pass — ask for the value.
- Do not commit or push; leave that to the human.
