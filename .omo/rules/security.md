# Security Rules

## Secrets

- `.env` exists at the repository root and is gitignored.
- Do not read, print, summarize, or copy `.env` contents.
- If a task needs environment context, check only whether the file exists and ask for the specific non-secret value if truly required.
- Do not expose GitHub Actions secrets, AWS credentials, JWT secrets, OAuth credentials, mail credentials, S3 credentials, Redis passwords, or database passwords in logs or docs.

## Spring Configuration

- New config goes into `src/main/resources/application-<profile>.yml`, then is included deliberately from `application.yml`.
- Do not add secrets directly to `application.yml` or committed profile files.
- `spring-dotenv` is part of the current runtime; treat it as an existing project choice, not permission to inspect local secrets.

## Authentication And Authorization

- Authentication uses OAuth2/JWT and `authentication` module internals.
- Cross-module code should use public authentication contracts or annotations; do not reach into `authentication/internal` from another module.
- Tests should create JWT/cookies through `ApiTestSupport`, not by hardcoding token internals in each test.

## Infrastructure

- Deployment flows use ECR, EC2, nginx, S3, mail, Redis, and MySQL-related settings.
- `.github/workflows/release.yml` writes the GitHub secret `ENV_FILE` into `.env` on CI; never mirror that value locally in documentation.
- `compose-dev.yml` contains local development defaults; do not treat them as production secrets.

## Anti-Patterns

- Do not paste redacted-looking secrets that came from a real file; omit the value entirely.
- Do not commit generated local config, IDE secrets, or runtime state.
- Do not add logging of request headers/cookies/tokens outside test failure diagnostics already provided by RestAssured.
