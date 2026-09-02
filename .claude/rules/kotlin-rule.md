---
paths:
  - "**/*.kt"
---

# Kotlin is written as Kotlin — never as Java with Kotlin syntax

**Ask for a Kotlin class and you get idiomatic Kotlin.** Not a Java class with `fun`
instead of `void`. The target is the [JetBrains Kotlin coding
conventions](https://kotlinlang.org/docs/coding-conventions.html) and the
[idioms page](https://kotlinlang.org/docs/idioms.html) — what IntelliJ's own
*"Convert Java to Kotlin"* inspection would nag you about after the conversion.

This is not a stylistic preference to weigh against others. It applies to **every** `.kt`
file in this repo: new classes, edits to existing ones, and tests.

## The one rule

> When the task is "write a Kotlin class", the deliverable is idiomatic Kotlin.
> **Falling back to Java is the last resort**, taken only for the concrete blockers listed
> in [When Java is genuinely the answer](#when-java-is-genuinely-the-answer) — and when you
> take it, say *which* blocker forced it. Never silently downgrade to Java because the
> Kotlin version needed more thought.

Equally: don't machine-translate a Java sketch into Kotlin. Write the Kotlin you'd write if
Java didn't exist, then check it against the checklist below.

## Reach for the standard library first

Kotlin ships the abstraction. Use it instead of hand-rolling the Java shape.

| Instead of | Write | Why |
| --- | --- | --- |
| `try { … } finally { x.close() }` | `x.use { … }` | Closes on every path, returns the block's value |
| `if (x != null) { f(x) }` | `x?.let { f(it) }` | No smart-cast fight, no repeated `x` |
| `if (cond) x else null` | `x.takeIf { cond }` / `x.takeUnless { cond }` | Chains into `?:` and `?.let` |
| `if (x == null) throw …` then use `x` | `x ?: throw …` / `requireNotNull(x) { … }` | One expression, `x` is non-null after |
| `if (x == null) return d else x` | `x ?: d` | Elvis is the whole idiom |
| a builder called across 4 statements | `obj.apply { … }` | Receiver stays implicit; returns `obj` |
| a temp var only used to log/peek | `expr.also { LOG.info(…) }` | Keeps the expression a single value |
| `if/else if/else` on one subject | `when (subject) { … }` | Exhaustive over sealed/enum, compiler-checked |
| `for (i in list) if (p(i)) out += f(i)` | `list.filter(::p).map(::f)` | And `mapNotNull`, `firstOrNull`, `associateBy`, `groupBy`, `sumOf`, `partition` |
| `StringBuilder` concatenation | `"$a/$b"` templates, `joinToString()` | |
| `Optional<T>` as a field/return | `T?` | `Optional` is a Java-interop artifact, not a Kotlin type |
| `throw IllegalArgumentException` guard | `require(cond) { … }` / `check(cond) { … }` | |
| a class that is only data | `data class` | `equals`/`hashCode`/`toString`/`copy` for free |
| a class that is only a namespace for constants | `companion object` with `const val` | |
| a 2-arm sealed result hierarchy | this repo's `Result<T>` (`Result.ok` / `Result.failure`) | Already exists — see the reuse reviewer |

**Pick the right scope function** — they are not interchangeable, and picking by coin-flip
is its own kind of slop:

- `let` — transform a value, or run a block only when non-null (`?.let`)
- `run` — same, but with the value as receiver; also `run { }` for a scoped expression
- `apply` — configure a receiver, **return the receiver**
- `also` — side effect (log, register, validate), **return the receiver**
- `with(x) { }` — several calls on one non-null receiver
- `use` — anything `Closeable`/`AutoCloseable`

**Never use a scope function for nothing.** `x.let { f(it) }` where `x` is already non-null
is noise — write `f(x)`. There is a live example of exactly this misuse in
`UserServiceImpl.updateKeycloakUsername`:

```kotlin
// Current — `updateUserRequest` is a non-null parameter, so `let` buys nothing
val userRepresentation: UserRepresentation? = updateUserRequest.let { userMapper.updateUserKeycloak(it) }

// Idiomatic
val userRepresentation = userMapper.updateUserKeycloak(updateUserRequest)
```

## Checklist — the Java-isms that keep showing up here

Run through this before calling a `.kt` file done. Each line is a real pattern found in
this repo's Kotlin sources.

1. **`==`, not `.equals()`.** In Kotlin `==` *is* `equals()` with a null-safe guard.
   `UserServiceImpl.updateUser` currently has
   `updateUserRequest.username().equals(checkUsernameExist?.username)` — that is Java. Write
   `updateUserRequest.username() == checkUsernameExist?.username`.

2. **Don't declare a type that the compiler already knows**, and never declare a nullable
   type for a value that cannot be null. `val user: User? = repo.findX().orElseThrow { … }`
   is wrong twice: `orElseThrow` returns non-null, and the annotation is redundant. Write
   `val user = repo.findX().orElseThrow { … }` and let the `?.` disappear downstream.
   Explicit types on **public** declarations are fine and often good; inside a function body
   they are usually clutter.

3. **Immutable and read-only by default.** `val` over `var`; `List`/`Set`/`Map` over
   `MutableList`/`MutableSet`/`MutableMap` in every signature. `TodoController.save` takes
   `MutableList<TodoRequest>` — a controller never mutates the body, so it should be
   `List<TodoRequest>`.

4. **Null-safety is a design decision, not a spelling.** `@PathVariable username: String?`
   on an endpoint whose path segment is mandatory pushes a fake null through the whole call
   chain (and into `@PreAuthorize`). Declare `String` and let Spring reject the miss.
   `!!` is a bug report in advance — use `?:`, `requireNotNull`, or fix the type.

5. **Expression bodies for single-expression functions.**
   `fun of(request: HttpServletRequest): Tier = if (…) AUTH else API` — as `Tier.kt` already
   does. Not `{ return … }`.

6. **`if`/`when`/`try` are expressions.** Assign from them instead of pre-declaring a `var`
   and filling it in each branch.

7. **Constructor properties, not fields + `init`.** Spring injection is the primary
   constructor: `class UserServiceImpl(private val userRepository: UserRepository, …)`.
   No `@Autowired`, no `lateinit var` for dependencies.

8. **Top-level and extension functions over static-utility classes.** Kotlin has no
   `static`; a `object Utils { fun … }` holding stateless helpers is a Java habit. Prefer a
   top-level function in the relevant package, or an extension on the type it operates on.

9. **`companion object` for constants and loggers**, `const val` where the value is a
   compile-time constant. The logger convention here is
   `private val LOG = LoggerFactory.getLogger(X::class.java)` inside the companion object —
   see `RateLimitFilter` and `UserServiceImpl`.

10. **Trailing lambda goes outside the parens**, and `it` is fine for a short single
    parameter — name it when the lambda is longer than a line or two, or when nested.

11. **No Java-style getters/setters.** `response.status = 429`, not `response.setStatus(…)`,
    when the Java type exposes a bean property. Declare Kotlin properties, not
    `getFoo()`/`setFoo()` pairs.

12. **`sealed interface` + `when` instead of visitor/instanceof ladders**, and
    `enum class` with behaviour on it (`Tier` carries `newBucket()`) instead of a `switch`
    somewhere else.

13. **Named arguments and default values instead of overload chains.** One function with
    defaults beats four constructors.

14. **Nothing in `src/main/kotlin`.** `kotlin-maven-plugin` compiles `src/main/java`, and
    Spotless only includes `src/main/java/**/*.kt`. A file under `src/main/kotlin` is
    compiled by nothing and formatted by nothing. This is in
    [CLAUDE.md](../../CLAUDE.md) too — it is the single easiest way to ship a class that
    silently does not exist.

## Interop with the Java side of the monolith

The Java modules are not going away, so Kotlin here has to be a good neighbour:

- **Spring Data repositories return `Optional<T>`.** Consume it at the boundary — `.map { }`
  / `.orElseThrow { }` chains are acceptable *there* — but do not let `Optional` leak into a
  Kotlin signature. Convert to `T?` (`getOrNull()`) as soon as it crosses into your own code,
  and prefer defining new Kotlin repository methods to return `T?`.
- **Java types are platform types** (`String!`). Decide the nullability explicitly at the
  first Kotlin declaration that touches them rather than inheriting `!` through the code.
- **The `all-open` compiler plugin is configured with the `spring` preset**, so
  `@Component`/`@Service`/`@Configuration`/`@Transactional` classes are opened automatically
  — do **not** hand-write `open class` for Spring beans.
- **Spring/Jakarta annotations that take arrays** need Kotlin array literals:
  `@RequestMapping(value = ["/api/v1/mono/todo"])`, `@Transactional(rollbackFor = [NotFoundException::class])`.
- The ArchUnit rules in `rest/src/test` do not care which language a class is written in.
  **Every public method of a `*ServiceImpl` still needs `@Transactional`**, Kotlin included.

## When Java is genuinely the answer

There is no annotation processing for Kotlin in this build — **no `kapt`, no KSP**. The
`maven-compiler-plugin`'s `annotationProcessorPaths` (MapStruct + Lombok) runs over Java
sources only. So these stay Java, and that is the *whole* list:

| Must stay Java | Because |
| --- | --- |
| MapStruct mappers (`service/mapper/**`) | The processor never sees `.kt` sources — a Kotlin `@Mapper` interface generates no implementation and fails at runtime |
| Lombok-annotated types — JPA entities (`persistence/entity/**`), anything using `@Slf4j`/`@Builder`/`@RequiredArgsConstructor` | Same processor gap. In Kotlin you don't need Lombok anyway: use `data class`, constructor properties, and a companion-object logger |
| Generated OpenAPI DTOs and API interfaces | Emitted by `openapi-generator-maven-plugin`; edit `openapi/json-api-holder.yaml`, never the output |

Anything else — controllers, filters, services and their impls, configuration classes,
enums, hand-written DTOs, repositories, tests — is fair game for idiomatic Kotlin, and the
repo already has working examples of each. If you think you have found a fourth blocker,
say so explicitly and explain it; don't quietly produce a Java file instead.

## Verify

```sh
mvn spotless:apply          # ktlint, with this repo's import layout — run before anything else
mvn clean test              # ArchUnit + the rest; Spotless check binds to validate
```

Spotless runs **ktlint** with `ij_kotlin_imports_layout = *,java.**,javax.**,kotlin.**,^`.
It fixes formatting; it does not fix Java-shaped Kotlin. That part is on you.
