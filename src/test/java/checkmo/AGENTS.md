# CHECKMO TESTS

## OVERVIEW

Tests are integration-heavy and run under the `test` profile. API tests use a real random-port Spring Boot server with RestAssured and H2; Modulith boundaries are verified separately.

## WHERE TO LOOK

| Task | Location | Notes |
| --- | --- | --- |
| Modulith boundary test | `CheckmoApplicationTests.java` | Runs `ApplicationModules.of(CheckmoApplication.class).verify()`. |
| HTTP API test meta-annotation | `support/ApiTest.java` | `RANDOM_PORT`, `@ActiveProfiles("test")`, `@Tag("integration")`. |
| Non-web Spring test meta-annotation | `support/SpringTest.java` | `webEnvironment = NONE`, same test profile/tag. |
| Shared API fixtures | `support/ApiTestSupport.java` | Test users, JWT cookies, H2 cleanup, mocked Redis/S3/mail/schedulers. |
| API test rationale | `docs/api-test/README.md` | Explains H2, RestAssured, fixture, external boundary strategy. |
| API coverage plan | `docs/api-test/coverage-matrix.md` | Use when expanding endpoint coverage. |

## CONVENTIONS

- Use `ApiTestSupport` for REST API tests that need the actual filter chain, JWT cookies, or HTTP status/body assertions.
- Use `@SpringTest` for non-HTTP Spring integration tests.
- Keep API test names with the `*ApiTest` suffix.
- Prefer behavior-specific test method names over generic numbered names.
- Mock external boundaries through the shared support layer when the boundary is common across API tests.
- Tests run with `src/test/resources/application.yml` activating only the `test` profile.
- Current API-test DB strategy is H2 with Hibernate schema creation; Flyway/MySQL migration compatibility is a separate future layer, not active Testcontainers coverage.

## COMMANDS

```bash
./gradlew test --tests checkmo.CheckmoApplicationTests
./gradlew test --tests checkmo.support.ApiTestInfrastructureTest
./gradlew test --tests 'checkmo.<package>.<ClassName>'
./gradlew test --tests '*Api*'
./gradlew test
```

## ANTI-PATTERNS

- Do not read `.env` to make tests pass.
- Do not activate production profiles (`prod`, `redis`, `aladin`, `mail`, `jwt`, `oauth2`, `s3`) from tests.
- Do not weaken or remove `ApplicationModules.verify()` when a module boundary fails.
- Do not add network, Redis, S3, mail, or scheduler dependencies to API tests unless the test explicitly owns that boundary.
- Do not leave persistent test data; rely on `ApiTestSupport` cleanup or add local cleanup for new tables.
