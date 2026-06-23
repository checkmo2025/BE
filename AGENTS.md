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

## WORKFLOW RULES

### Java Imports And Type Names

- Do not reference standard Java collection types with fully qualified names in code bodies, such as `java.util.List`.
- Import the type and use the short form, such as `List`, `Map`, or `Set`.
- Do not create new classes, records, DTOs, or nested types whose names collide with standard Java collection names.
- If a naming conflict appears, rename the project type instead of forcing fully qualified collection references.

### Branch Naming

- For GitHub issue-based work, use a branch name that starts with the work type:
  - `feat/{issue-number}/{work-summary}`
  - `refactor/{issue-number}/{work-summary}`
  - `fix/{issue-number}/{work-summary}`
  - `test/{issue-number}/{work-summary}`
  - `docs/{issue-number}/{work-summary}`
- Write `{work-summary}` in short English kebab-case.
- Example: `feat/247/sitemap-metadata-apis`.

### DTO And Mapper Decisions

- When adding a DTO, inspect the owning module's existing converter/mapper pattern first.
- If the module already uses a converter or mapper for similar response construction, prefer adding the new DTO mapping there instead of spreading DTO assembly through controllers or services.
- Do not add a mapper only for ceremony when a simple repository projection or existing local pattern is clearer.

### Stage-Based Work And Commits

- Split work into the smallest practical stages so the user can keep understanding the code as it changes.
- Each stage should have one clear purpose, such as a shared DTO, one module endpoint, one focused test change, or one documentation update.
- Avoid changing multiple modules in the same stage when the work can be split by module.
- After completing each stage, report:
  - changed files
  - summary of changes
  - verification result
  - remaining risk
  - recommended commit message
- Wait for the user's confirmation before committing that stage, then continue to the next stage.
- Keep commits as close to one stage per commit as practical.
- Prefer small commits, but do not create commits that leave the project uncompilable or contain meaningless micro-changes.
- Each commit should be understandable as a standalone review unit.
- Use Conventional Commit style with a Korean summary unless the user requests otherwise.

### Local Server Runtime Configuration

- Only for local server runs, apply these temporary config changes:
  - In `application.yml`, change the active runtime profile from `prod` to `db`.
  - In `application-redis.yml`, uncomment the section marked for the local development environment.
  - In `application-redis.yml`, comment out `host: ${REDIS_ENDPOINT}` while using the local Redis container.
  - Start MySQL and Redis with Docker before running the app locally.
- Never commit these temporary runtime config changes.
- Before finishing, restore the original config:
  - active runtime profile uses `prod`
  - the local development Redis section is commented again
  - `host: ${REDIS_ENDPOINT}` is active again
- Do not read, print, or summarize `.env` or any secret-bearing file while doing local runtime setup.

### Build Artifact Cleanup

- Delete obvious build artifacts or temporary duplicate outputs whose names end with a space and a number.
- Examples: `build 2`, `generated 3`, `SomeFile 2.class`.
- Apply this aggressively inside build output or generated-output areas such as `build`, `out`, `target`, `.gradle`, or generated source output.
- Do not silently delete source, documentation, or configuration files only because their names end with a space and a number; report those first.

## API VERSIONING

- All external client-facing HTTP APIs must use the `/api/v1/...` prefix.
- Do not add new unversioned `/api/...` endpoints.
- Before public users exist, migrate existing `/api/...` endpoints destructively to `/api/v1/...`; do not keep compatibility aliases unless explicitly requested.
- Keep non-client operational endpoints such as `/health`, `/swagger-ui/**`, `/v3/api-docs/**`, OAuth callback paths, and websocket broker paths outside this versioning rule unless they become external client API contracts.
- Within the current major version, allow only non-breaking API changes such as optional fields, new endpoints, and backward-compatible enum additions.
- Treat endpoint removal or path changes, required request field additions, response field removal/rename/semantic changes, status/error code contract changes, and authentication/token delivery changes as breaking changes that require a new major prefix such as `/api/v2/...` or `/api/v3/...`.
- When adding a new major API version, keep existing major-version endpoints such as `/api/v1/...` available until an explicit deprecation and removal plan is approved.
- Do not silently repoint existing versioned endpoints to incompatible behavior; expose the incompatible contract under the new major prefix and keep the old contract stable.
- When changing API paths, update controller mappings, Spring Security matchers, JWT/profile filter path rules, Swagger/OpenAPI descriptions, API tests, and API docs together.

## COMMIT MESSAGE STYLE

Use the repository's existing commit message style.

Default format:
- Use Conventional Commit style: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`, `ci:`, `hotfix:`.
- Prefer a Korean summary after the type.
- Add a scope when it improves clarity, especially for module or infrastructure-specific changes:
  - `feat(report): 신고 모듈 분리 및 공통 신고 기능 추가`
  - `fix(websocket): allowedOrigins 설정 와일드카드 하드코딩 제거`
  - `docs: API 테스트 문서 추가`

For simple changes, a single-line commit message is enough:

```text
fix: 프로필 조회시 이름 반환 추가
```

For larger, review-driven, operational, or architecture-sensitive changes, include a concise body that explains why the change was made and what was verified. Prefer these trailers when they add useful decision context:

```text
<type>(<optional-scope>): <Korean summary>

<short body explaining intent and important constraints>

Constraint: <external constraint or issue/review context>
Rejected: <alternative considered> | <reason>
Confidence: <low|medium|high>
Scope-risk: <narrow|moderate|broad>
Directive: <future maintenance warning>
Tested: <commands or checks run>
Not-tested: <known verification gaps>
```

Rules:
- The subject should describe the intent clearly, not just list files.
- Do not invent verification. `Tested:` must match commands actually run.
- Use `Not-tested:` for honest gaps.
- Do not include secrets, `.env` values, DSNs, tokens, or raw production payloads in commit messages.
- Merge commits may keep GitHub's generated merge title.

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
