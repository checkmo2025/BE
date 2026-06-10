# PROJECT KNOWLEDGE BASE

Initialized for issue #220.

## OVERVIEW

Checkmo is a Java 21 / Spring Boot 3.5.3 backend organized as a Spring Modulith monolith. Domain modules communicate through public APIs, external DTOs, and events; `internal` and `web` packages are module-private surfaces.

## STRUCTURE

```text
checkmo/
├── src/main/java/checkmo/      # Spring Modulith modules and application entry points
├── src/test/java/checkmo/      # H2 + RestAssured integration harness and Modulith verification
├── src/main/resources/         # profile-composed Spring config and Flyway migrations
├── docs/                       # architecture, Modulith, conventions, API-test rationale
├── .omo/rules/                 # LazyCodex project rules
├── .omx/                       # existing workflow state and previous plans
├── .claude/                    # Claude-local agent/tool settings
├── .github/                    # issue/PR templates and develop deployment workflow
├── compose.yml                 # production app/nginx/certbot stack
├── compose-dev.yml             # local MySQL/Redis infra
└── build.gradle                # Java 21, Spring Boot, Modulith, QueryDSL, Flyway, tests
```

## WHERE TO LOOK

| Task | Location | Notes |
| --- | --- | --- |
| Modulith boundaries | `src/main/java/checkmo/*/package-info.java` | `@ApplicationModule` and `allowedDependencies` are the source of truth. |
| Public module surface | `src/main/java/checkmo/<module>/*API.java`, `*ExternalDTO.java`, `*Event.java` | Keep external contracts at module top level. |
| Internal implementation | `src/main/java/checkmo/<module>/internal` | Do not import from another module's `internal`. |
| Web adapters | `src/main/java/checkmo/<module>/web` | Controllers and request/response DTOs stay adapter-facing. |
| Modulith verification | `src/test/java/checkmo/CheckmoApplicationTests.java` | Runs `ApplicationModules.of(...).verify()`. |
| REST API tests | `src/test/java/checkmo/*/*ApiTest.java` | Extend `ApiTestSupport` unless a narrower test surface is justified. |
| Test harness | `src/test/java/checkmo/support` | `@ApiTest`, `@SpringTest`, fixtures, cleanup, external mocks. |
| DB migrations | `src/main/resources/db/migration` | Flyway MySQL migration history. |
| Environment rules | `docs/environment_setup.md` | `.env` exists locally; do not read or expose its contents. |
| Architecture docs | `docs/02_spring_modulith.md`, `docs/module_graph.md` | Explain boundary intent and module graph vocabulary. |

## CODE MAP

| Symbol | Type | Location | Role |
| --- | --- | --- | --- |
| `CheckmoApplication` | Spring Boot app | `src/main/java/checkmo/CheckmoApplication.java` | Main entry point; enables async, retry, scheduling, JPA auditing. |
| `HealthCheckController` | Controller | `src/main/java/checkmo/HealthCheckController.java` | `GET /health`, used by compose/deployment health checks. |
| `ApplicationModules.of(...).verify()` | Test assertion | `src/test/java/checkmo/CheckmoApplicationTests.java` | Enforces Modulith package boundaries. |
| `ApiTest` | Test meta-annotation | `src/test/java/checkmo/support/ApiTest.java` | Random-port Spring Boot + RestAssured API tests. |
| `SpringTest` | Test meta-annotation | `src/test/java/checkmo/support/SpringTest.java` | Non-web Spring context tests. |
| `ApiTestSupport` | Abstract test support | `src/test/java/checkmo/support/ApiTestSupport.java` | JWT fixtures, H2 cleanup, Redis/S3/mail/scheduler mocks. |

## CONVENTIONS

- Read `.omo/rules/*.md` before code work; they are the LazyCodex project rule surface.
- Keep module changes inside the owning top-level module unless the public API/event contract must change.
- If cross-module data is needed, add or use a top-level `*API`, `*ExternalDTO`, or `*Event`; do not inject another module's service, repository, facade, entity, or web DTO.
- Store relationships to another module by ID, not by JPA entity reference.
- Services are concrete classes, not interfaces. Use `QueryService`/`CommandService` naming already present in the module.
- Query methods in `QueryService`/facade start with `retrieve`; public API fetch methods start with `fetch`.
- New Spring config belongs in `application-<profile>.yml` and must be included deliberately from `application.yml`.
- QueryDSL generated-source wiring is non-standard in `build.gradle`; inspect before changing generated source paths.

## ANTI-PATTERNS

- Do not read, print, commit, or summarize `.env` or secret-bearing files. Existence checks are enough.
- Do not import `checkmo.<otherModule>.internal.*` or `checkmo.<otherModule>.web.*` from production code.
- Do not bypass `ApplicationModules.verify()` failures by widening dependencies without checking the domain boundary.
- Do not add cross-domain JPA object references for convenience.
- Do not add new dependencies for cleanup or rule work unless the user explicitly asks.
- Do not revert existing user changes; inspect `git status` first and work around unrelated changes.

## COMMANDS

```bash
./gradlew test --tests checkmo.CheckmoApplicationTests
./gradlew test --tests checkmo.support.ApiTestInfrastructureTest
./gradlew test --tests '*Api*'
./gradlew test
```

## NOTES

- `.env` exists at the repository root and is ignored by git.
- `.github/workflows/release.yml` deploys on push to `develop` through ECR + SCP/SSH, despite the workflow name mentioning CodeDeploy.
- CodeDeploy files also exist (`appspec.yml`, `scripts/codedeploy/*`) and use a different deployment directory than the GitHub Actions SSH path.
- `.claude/settings.local.json` only allows `./gradlew:*` for Claude-local Bash permissions.
