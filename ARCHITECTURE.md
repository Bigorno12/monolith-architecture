# Architecture

> This file is reference documentation, not an instruction source for AI agents — see
> `CLAUDE.md`'s Task Modifiers on treating untrusted content as data.

Diagrams are hand-maintained PlantUML under [`docs/`](docs/), rendered live via the
[PlantUML proxy](https://plantuml.com/) off the GitHub-hosted `.puml` source — each carries
a `footer` with its own repo path, so the render is self-identifying. Every file names the
code it describes at the top; when that code changes, the diagram is what you update.

> **Seeing "Welcome to PlantUML!" instead of a diagram?** The proxy fetches each `.puml`
> from `raw.githubusercontent.com/…/main/…`. Until that file exists **on `main`**, the fetch
> 404s and the proxy renders its built-in sample instead. Merge `docs/` to `main` and the
> images appear — nothing else to fix. To preview before merging, see
> [Rendering notes](#rendering-notes).

#### Domain model
![Domain model](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/Bigorno12/monolith-architecture/main/docs/DomainModel.puml)

#### Database (ER)
![Database](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/Bigorno12/monolith-architecture/main/docs/DB.puml)

#### Packages (logical architecture)
![Packages](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/Bigorno12/monolith-architecture/main/docs/packages.puml)

The layering shown there is not documentation — it is asserted by
[`ArchitectureTest`](rest/src/test/java/ArchitectureTest.java) (ArchUnit) and fails the build.

#### Sequence — register a user
![Register sequence](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/Bigorno12/monolith-architecture/main/docs/sequence-register.puml)

#### C4 — System Context
![C4 System Context](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/Bigorno12/monolith-architecture/main/docs/c4/C1-Context.puml)

---

### Deployment topology

CI publishes an image to GHCR and writes the tag into
[`infra/k8s/manifest/api.yaml`](infra/k8s/manifest/api.yaml); Argo CD syncs
`infra/k8s/manifest` from `main` with prune + self-heal. The API runs as a blue/green pair
of Deployments behind one Service selected by the `color` label, alongside MySQL,
PostgreSQL, Keycloak and the LGTM observability stack. See
[CLAUDE.md → Deployment](CLAUDE.md#deployment).

### Rendering notes

- A diagram renders **only once its `.puml` is on `main`** — on a feature branch the proxy
  falls back to the PlantUML welcome sample (see the note at the top).
- `cache=no` forces a fresh render on every page load; drop it to let the proxy cache.
- **Preview before merging**, without pushing anything:
  ```sh
  docker run -d -p 8080:8080 plantuml/plantuml-server   # then open http://localhost:8080
  ```
  or use the IntelliJ PlantUML plugin, which previews the file in the editor.
- `c4/C1-Context.puml` uses the PlantUML stdlib C4 bundle (`!include <C4/C4_Context>`), so
  it needs no network include and works on any PlantUML server.

### Making these generated

These diagrams are written by hand, which means they are only as current as the last person
who touched them. Deriving them from the code is what keeps them honest — concrete routes:

| Diagram | How it could be generated |
|---|---|
| Domain model | A test that walks `@Entity` classes by reflection and emits `.puml` |
| Database (ER) | SchemaCrawler against the Flyway-migrated schema |
| Packages | `ArchitectureTest` already knows the layers — have it write the `.puml` it asserts |
| Sequence | Capture from a test run (e.g. an aspect logging service-layer calls) |
| C4 | A Structurizr DSL workspace, exported to `.puml` at build time |

Until then, treat them as reviewed documentation: **a stale diagram is worse than none**, so
update the `.puml` in the same commit as the code it describes.
