# Testing Rules

## Default Strategy

- Default verification command is `./gradlew test`.
- For narrow changes, run the closest targeted test first, then widen only as risk grows.
- For module-boundary or package dependency changes, run `./gradlew test --tests checkmo.CheckmoApplicationTests`.
- For API harness or fixture changes, run `./gradlew test --tests checkmo.support.ApiTestInfrastructureTest` before broader API tests.

## Test Surfaces

- REST API tests use `ApiTestSupport`, `@ApiTest`, RestAssured, a random port, and the actual Spring Security/JWT filter chain.
- Non-HTTP Spring integration tests use `@SpringTest`.
- `@ApiTest` and `@SpringTest` both carry `@Tag("integration")` and `@ActiveProfiles("test")`.
- Test profile config is under `src/test/resources`.

## Data And Boundaries

- H2 is the active integration-test database and runs close to MySQL mode.
- API tests mock Redis, S3, mail, scheduler, and selected external API boundaries through `ApiTestSupport`.
- `ApiTestSupport` clears H2 tables after each test; add cleanup only when introducing tables outside that path.
- No active Testcontainers suite exists in this repo; docs mention it only as a future migration-compatibility layer.

## Commands

```bash
./gradlew test --tests checkmo.CheckmoApplicationTests
./gradlew test --tests checkmo.support.ApiTestInfrastructureTest
./gradlew test --tests '*Api*'
./gradlew test
```

## Anti-Patterns

- Do not use production profiles or real `.env` secrets in tests.
- Do not bypass the shared test harness for API tests without a clear reason.
- Do not remove assertions or weaken fixtures to make a suite green.
- Do not turn external services into required local dependencies for ordinary API tests.
