# Spring Modulith Rules

## Boundary Source Of Truth

- Every top-level package under `src/main/java/checkmo` is a Spring Modulith application module.
- Each module's `package-info.java` declares its `@ApplicationModule` and `allowedDependencies`.
- `src/test/java/checkmo/CheckmoApplicationTests.java` verifies the module graph with `ApplicationModules.of(CheckmoApplication.class).verify()`.

## Public Surface

- Cross-module synchronous calls go through top-level `*API.java`.
- Cross-module shared data uses top-level `*ExternalDTO.java`.
- Cross-module asynchronous side effects use top-level `*Event.java` and Spring events.
- API implementations live inside the owning module's `internal` package.

## Private Surface

- `internal` is private implementation: service, repository, entity, converter, listener, exception, scheduler, validation, and API implementation code.
- `web` is private adapter code: controllers and web request/response DTOs.
- Production code in one module must not import another module's `internal` or `web` package.

## Persistence Coupling

- Same-module JPA relationships are allowed when they fit the domain.
- Cross-module entity object references are forbidden; store foreign module IDs instead.
- If read-side composition needs foreign module data, fetch it through the foreign module's public API.

## Events

- Use events when the publishing module should not know which module reacts.
- Include a stable event/source ID when retry or duplicate delivery is possible.
- Event listeners that create side effects must be idempotent.
- Existing docs describe retry behavior in `docs/02_spring_modulith.md`; check that before changing event retry logic.

## Verification

```bash
./gradlew test --tests checkmo.CheckmoApplicationTests
./gradlew test
```

Run the targeted Modulith test after any package move, new dependency, public API change, or event contract change.
