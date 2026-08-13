---
paths:
  - "**/pom.xml"
---

# Maven versions: let the root POM resolve them

**Never hardcode a `<version>` that the parent POM or an imported BOM already manages.**
A redundant `<version>` silently pins the artifact, so the next parent upgrade stops
applying to it — the dependency drifts out of the tested, coherent set the BOM guarantees.

This repo is a **four-POM reactor**: `pom.xml` (root, parent
`spring-boot-starter-parent` 4.1.0) → `persistence` → `service` → `rest`.
**All versions live in the root POM.** A module POM lists `groupId` + `artifactId`
(+ `scope`) and nothing else — that is the current state of all three, keep it that way.

## Rules

1. **Managed artifact → omit `<version>` entirely.** If `dependencyManagement` (inherited
   from Boot, imported from a BOM, or declared in the root POM) resolves the artifact, the
   dependency block is just `groupId` + `artifactId` (+ `scope`). Same for `<plugin>`
   versions covered by `pluginManagement`.

2. **To change a Boot-managed version, override the parent's property — do not add
   `<version>`.** `spring-boot-starter-parent` declares its versions through properties, so
   redefining the property in the root `<properties>` is the sanctioned override and keeps
   one source of truth:

   ```xml
   <properties>
     <commons-codec.version>1.22.1</commons-codec.version>  <!-- overrides Boot's 1.21.0 -->
   </properties>
   ```

   This works even when the artifact appears nowhere in this repo's POMs — it retargets a
   *transitive* dependency. Four properties here exist purely for that (see below).

3. **Unmanaged artifact → add it to the root `dependencyManagement` with a property-backed
   version**, never inline in a module POM:

   ```xml
   <properties>
     <springdoc-openapi-starter-webmvc-ui.version>3.1.0</springdoc-openapi-starter-webmvc-ui.version>
   </properties>
   ...
   <dependencyManagement><dependencies>
     <dependency>
       <groupId>org.springdoc</groupId>
       <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
       <version>${springdoc-openapi-starter-webmvc-ui.version}</version>
     </dependency>
   </dependencies></dependencyManagement>
   ```

4. **Never invent a version number.** If you cannot confirm the artifact is unmanaged,
   check before writing anything (see below).

## Two build gates that punish getting this wrong

Both run at **`initialize`/`validate`, before compilation**, so a bad edit fails everything:

- **`maven-enforcer-plugin`** — `dependencyConvergence` + `requireUpperBoundDeps` +
  `requirePluginVersions`. Adding a dependency whose transitives disagree with an existing
  one fails the build. The fix is a pin in the **root** `dependencyManagement`, never a
  `<version>` in the module that happens to complain.
- **`spotless` (`sortPom`)** — `pom.xml` is formatted and `sortProperties=true`, so
  `<properties>` must stay **alphabetically sorted**, 4-space indent, empty elements
  unexpanded. Don't hand-align: add the property and run `mvn spotless:apply`.

Dependabot also opens weekly Maven PRs that edit exactly these properties (`resteasy.version`,
`swagger-annotations.version`, … — see the recent history). Prefer letting it do the bumping;
a hand-edit to the same property conflicts with its open PR.

## Check before you add a `<version>`

```sh
./mvnw -o help:evaluate -Dexpression=project.dependencyManagement -DforceStdout \
  | grep -A2 '<artifactId>THE_ARTIFACT</artifactId>'
```

Output → managed: **omit** the version (or override the property).
No output → unmanaged: add it to the root `dependencyManagement`, property-backed.

To see whether Boot itself defines a property before you override it:

```sh
grep -oE '<THE.version>[^<]*<' ~/.m2/repository/org/springframework/boot/\
spring-boot-dependencies/4.1.0/spring-boot-dependencies-4.1.0.pom
```

Then confirm nothing broke: `./mvnw -q -o initialize` (runs the enforcer alone, fast).

## State of this repo

**Managed by the Boot 4.1.0 parent — never give these a version:** anything
`spring-boot-starter-*` / `spring-*`, `com.github.ben-manes.caffeine:caffeine`,
`org.projectlombok:lombok`, `com.mysql:mysql-connector-j`, `com.h2database:h2`.

**Managed by this repo's root `dependencyManagement`** — module POMs omit the version:
MapStruct, swagger-annotations, springdoc, jakarta.validation-api, the five Blaze-Persistence
artifacts, bucket4j, the three RESTEasy artifacts, commons-io, resilience4j-spring-boot3,
datasource-proxy, ArchUnit, and the Testcontainers artifacts (mysql / postgresql /
junit-jupiter / keycloak). `kotlin-bom` is imported, so **every** Kotlin artifact is managed
by it.

Known deviations, worth fixing when you are next in the file:

| where | issue | correct form |
| --- | --- | --- |
| root `<dependencies>` → `org.projectlombok:lombok` | carries `<version>${lombok.version}</version>`, but Boot already manages lombok (1.18.46) | drop the `<version>` line; override `lombok.version` in `<properties>` only if you need a different one |
| root `dependencyManagement` → `kotlin-reflect` | pinned to `${kotlin.version}` although `kotlin-bom` is imported right above it | drop the entry; the BOM covers it |
| `<properties>` → `build-helper-maven-plugin.version` = 3.5.0 | Boot 4.1.0 ships **3.6.1** — this override is a silent **downgrade** | raise it or delete the property |
| `<properties>` → `commons-lang3.version` = 3.20.0 | identical to Boot's 3.20.0 — a no-op today that will pin the artifact the moment Boot moves | delete unless you deliberately want it frozen |

The other two overrides are doing real work and should stay: `commons-codec.version` 1.22.1
(Boot: 1.21.0) and `jackson-2-bom.version` 2.22.0 (Boot: 2.21.4). Neither artifact is
declared anywhere in these POMs — the properties exist solely to lift the transitive versions.
