# Domain Conventions

## Module List

Primary modules in this repo:

- `authentication`
- `book`
- `bookStory`
- `clubManagement`
- `clubMeeting`
- `clubNotice`
- `common`
- `infra`
- `member`
- `news`
- `notification`
- `realtime`
- `report`

## Module Shape

- Top-level files are public contracts: `*API`, `*ExternalDTO`, `*Event`, module annotations, and `package-info.java`.
- `internal` contains domain implementation and persistence details.
- `web` contains HTTP adapters and web DTOs.
- `common` is declared as an open module and is the shared utility/base surface.

## Naming

- `QueryService` and query facade methods start with `retrieve`.
- Public API fetch methods start with `fetch`.
- DTO container classes end with `RequestDTO` or `ResponseDTO`.
- Nested DTO types do not repeat `Request`, `Response`, `DTO`, or `Dto`.
- Map-returning method names end with `By<Key>` or `By<Keys>` to describe the key.
- State-flip methods start with `toggle`.

## Service And Facade Roles

- Services are concrete classes; avoid service interfaces unless the project already has a clear local reason.
- Entities own local state transitions and invariants for data they already hold.
- Services own entity lookup, repository-driven uniqueness/existence checks, transactions, persistence orchestration, event publication, logging, cross-module API calls, and application flow composition.
- Services may coordinate business use cases, but they should not keep procedural branches for rules that naturally belong to the loaded domain object.
- If a service must call multiple domain methods in a specific order to complete one business action, move that sequence behind a single intention-revealing domain method when the domain object has the required data.
- Facades compose services, public APIs, converters, paging, filtering, and DTO assembly.
- Converters keep DTO/entity mapping out of services.

## Cross-Module Work

- First check the caller's `allowedDependencies`.
- Prefer an existing public API/event before widening a dependency.
- If a new public API is needed, expose the smallest stable DTO that avoids leaking internals.
- If an event is needed, put the event at module top level and make listeners idempotent when retries are possible.
- Do not return entities, repositories, services, facades, or web DTOs across module boundaries.

## Database

- Flyway migrations live in `src/main/resources/db/migration`.
- Same-module entity relationships may use JPA object mapping.
- Cross-module relationships store IDs and compose data through APIs at the application layer.
- QueryDSL generated classes are generated under the project's Gradle configuration; inspect `build.gradle` before changing generated paths.
