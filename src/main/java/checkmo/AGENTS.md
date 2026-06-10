# CHECKMO APPLICATION MODULES

## OVERVIEW

This tree is the Spring Modulith application surface. Top-level packages under `checkmo` are application modules; each module's `package-info.java` declares its allowed dependencies.

## STRUCTURE

```text
checkmo/
├── authentication/     # auth, OAuth2/JWT, current member resolution
├── book/               # book lookup, Aladin integration, book social actions
├── bookStory/          # book story domain
├── clubManagement/     # clubs and membership
├── clubMeeting/        # meetings, bookshelf, topics, reviews
├── clubNotice/         # notices, comments, votes
├── common/             # open shared base/config/payload/template code
├── infra/              # email, S3, scheduling infrastructure
├── member/             # member profile, follow/block lifecycle
├── news/               # news and carousel domain
├── notification/       # notification settings and event listeners
├── realtime/           # websocket/realtime chat and presentation flows
└── report/             # reporting flows
```

## WHERE TO LOOK

| Task | Location | Notes |
| --- | --- | --- |
| Module dependency contract | `<module>/package-info.java` | Update only with a clear boundary reason. |
| Public synchronous API | `<module>/*API.java` | Methods generally use `fetch...` naming. |
| Public shared DTO | `<module>/*ExternalDTO.java` | Expose only data needed by other modules. |
| Public events | `<module>/*Event.java` | Prefer event publication for async cross-module side effects. |
| Internal API implementation | `<module>/internal/*APIImpl.java` | Implementation stays inside `internal`. |
| HTTP controllers | `<module>/web/controller` | Adapter layer; may call own module internals. |
| Request/response DTOs | `<module>/web/dto` | Do not use these as cross-module contracts. |

## MODULE DEPENDENCIES

| Module | Declared dependencies |
| --- | --- |
| `authentication` | `common` |
| `book` | `common`, `authentication`, `member` |
| `bookStory` | `authentication`, `book`, `clubManagement`, `common`, `member` |
| `clubManagement` | `authentication`, `book`, `common`, `member` |
| `clubMeeting` | `authentication`, `book`, `clubManagement`, `common`, `member` |
| `clubNotice` | `authentication`, `clubManagement`, `clubMeeting`, `common`, `member` |
| `common` | open module |
| `infra` | `authentication`, `member`, `clubManagement`, `clubNotice`, `common` |
| `member` | `authentication`, `common` |
| `news` | `authentication`, `common`, `member` |
| `notification` | `authentication`, `bookStory`, `clubManagement`, `clubMeeting`, `clubNotice`, `common`, `member` |
| `realtime` | `authentication`, `clubManagement`, `clubMeeting`, `common`, `member` |
| `report` | `authentication`, `member`, `bookStory`, `clubManagement`, `clubNotice`, `clubMeeting`, `realtime`, `common` |

## CONVENTIONS

- Top-level module package contains only public contracts: `*API`, `*ExternalDTO`, `*Event`, annotations, and `package-info.java`.
- `internal` contains entities, repositories, services, converters, listeners, exceptions, schedulers, validators, and API implementations.
- `web` contains controllers and web DTOs.
- Same-module code can use its own `internal`; cross-module production code must use public contracts only.
- Cross-module persistence references store IDs, not foreign module entity objects.
- Event listeners use Spring Modulith listener patterns and must be idempotent when events can be retried.

## ANTI-PATTERNS

- Importing another module's `internal` or `web` package from production code.
- Returning internal entities or web DTOs from public APIs.
- Adding `allowedDependencies` entries as a shortcut before checking whether an event or existing API fits.
- Creating JPA `@ManyToOne` or object references to another module's entity.
- Moving generated QueryDSL classes by hand; generated output is managed by Gradle configuration.
