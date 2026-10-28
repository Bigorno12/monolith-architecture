---
name: grill-me
description: Interviews you relentlessly about a plan, one question at a time, until you share an understanding. Invoked via /grill-me.
disable-model-invocation: true
---

Interview me relentlessly about every aspect of this plan until we reach a shared understanding.
Walk down each branch of the design tree, resolving dependencies between decisions one by one.
For each question, give your recommended answer.

Ask the questions one at a time, waiting for my answer before continuing. Asking several at
once is bewildering.

If a question can be answered by exploring the environment (code, build, git history, the
docs: CLAUDE.md, ARCHITECTURE.md's decision table, GUARDRAILS.md), look it up instead of
asking. The decisions, though, are mine: ask each one and wait for my answer.

In this repo, these branches always deserve a question when the plan touches them:
- **Layers.** Which module owns it: `rest`, `service` or `persistence`? Does it respect
  `rest → service → persistence`, and does a shared enum belong in `persistence.enumeration`?
- **Language.** Kotlin by default. If Java, which annotation-processor blocker forces it?
- **Transactions.** Is every public `*ServiceImpl` method `@Transactional`? Does anything touch
  a lazy association outside the transaction (`open-in-view=false`)?
- **Security.** Which `@PreAuthorize` authority, and is there an ownership check? Does it
  change `Permission` (fans out to persistence, `SecurityConfig`, every controller)? Which
  filter chain serves it?
- **Cross-cutting.** A new cache name (both `CaffeineConfig` and `spring.cache.cache-names`)?
  A circuit breaker and its `fallback*`? Rate-limit tier? `X-API-Version`?
- **Contract.** Does `openapi/json-api-holder.yaml` change, and with it the generated DTOs/clients?
- **Schema.** A new `V1_N__*.sql` Flyway migration, never an edit to an applied one.
- **Deployment.** Does it touch `infra/k8s/manifest` (Argo CD self-heals live edits) or need a
  new secret in `infra/.env.example` + `bootstrap-gitops.sh`?
- **Decisions.** Does the plan undo a row of ARCHITECTURE.md → Decisions? If so, say which
  context changed.
