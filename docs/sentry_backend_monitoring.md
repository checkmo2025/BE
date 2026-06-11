# Sentry Backend Monitoring

Issue: #222
Branch: `feat/222/sentry-monitoring`

## Scope

This rollout enables first-phase Sentry monitoring for the Spring Boot backend only. It is limited to unexpected 500-class exceptions handled by `ExceptionAdvice`.

Not included in this issue:
- tracing or performance monitoring
- Logback/Sentry log shipping
- async profiler
- realtime/websocket capture
- background listener capture
- mobile SDK work

## Environment Variables

Configure Sentry only through environment variables. Do not commit `.env`, DSNs, auth tokens, or event payloads.

```text
SENTRY_ENABLED=true|false
SENTRY_DSN=<non-prod or prod DSN>
SENTRY_ENVIRONMENT=staging|prod
SENTRY_RELEASE=checkmo-backend@<deployed git sha>
```

Default behavior is safe for local and test runs:
- `SENTRY_ENABLED=false`
- blank `SENTRY_DSN`
- no request body capture
- no default PII
- no tracing sample rate configured
- Sentry logs disabled

The application binds these values through `checkmo.sentry.*` properties and initializes the Sentry SDK only when `SENTRY_ENABLED=true` and `SENTRY_DSN` is not blank. The committed config intentionally does not set `sentry.dsn` and excludes every Sentry 8.43.1 Spring Boot auto-configuration import, so the starter cannot initialize independently of the kill switch.

In the current deployment flow, `.github/workflows/release.yml` creates `.env` from `secrets.ENV_FILE`, removes any stale `SENTRY_RELEASE`, appends `SENTRY_RELEASE=checkmo-backend@${GITHUB_SHA}`, and copies it to EC2 with `compose.yml`. Operators must update the GitHub `ENV_FILE` secret with `SENTRY_ENABLED`, `SENTRY_DSN`, and `SENTRY_ENVIRONMENT`; `SENTRY_RELEASE` is deployment-generated and should not be maintained manually in the secret. Do not edit, print, or commit the local `.env` file.

## Release and Deploy Tracking

Issue #232 adds CI-side Sentry release/deploy automation for the backend project. The GitHub Actions workflow creates a Sentry release named `checkmo-backend@<github sha>` after the EC2 deployment succeeds, then records a `prod` deploy for that release.

This automation uses `getsentry/action-release@v3` and requires this GitHub Actions secret:

```text
SENTRY_AUTH_TOKEN=<Sentry Internal Integration token>
```

`SENTRY_AUTH_TOKEN` is different from `SENTRY_DSN`:
- `SENTRY_DSN` is used by the running Spring Boot app to send events.
- `SENTRY_AUTH_TOKEN` is used only by GitHub Actions to create releases and deploy records through the Sentry API.
- Sentry `Client Secret` is not the CI auth token.

Do not add `SENTRY_AUTH_TOKEN` to `.env`, `ENV_FILE`, `compose.yml`, Spring configuration, or application runtime environment. Store it only as a GitHub Actions repository secret. The current token was created from a Sentry Internal Integration with release/CI-oriented scopes (`org:ci`, `org:read`, `project:read`, `project:releases`).

The Sentry organization and project slugs are non-secret workflow constants:

```text
SENTRY_ORG=checkmo
SENTRY_PROJECT=checkmo-spring-boot
```

The release name in Sentry must match the runtime SDK release value:

```text
checkmo-backend@<github sha>
```

The deployment workflow pins every third-party GitHub Action to a full commit SHA. When updating an action version, first resolve the intended upstream tag or branch to its current commit SHA, update the `uses:` line to that SHA, then rerun `SentryReleaseWorkflowTest`. Do not switch new or existing workflow actions back to mutable tags such as `@v3` or branches such as `@master`.

## Privacy Rules

The backend must not send these values to Sentry:
- PII
- `Authorization`
- `Cookie` / `Set-Cookie`
- JWT/access token/refresh token
- password fields
- verification or auth codes
- raw request bodies

The Sentry before-send callback removes user context, request bodies, cookies, sensitive headers, and sensitive query strings before an event can be sent.

Unexpected 500 API responses also avoid returning raw exception messages, so token-like exception messages are not echoed to clients.

## Expected Errors

These expected errors are not captured as Sentry issues by default:
- validation errors
- domain `GeneralException`
- authentication and authorization 401/403 paths
- `AccessDeniedException`
- `IllegalArgumentException`

Only the generic unexpected exception path is captured in this issue.

## Non-Prod Verification

1. Create or select a non-prod Sentry project.
2. Add a non-prod DSN to GitHub `ENV_FILE`:

   ```text
   SENTRY_ENABLED=true
   SENTRY_DSN=<non-prod DSN>
   SENTRY_ENVIRONMENT=staging
   ```

3. Deploy from `develop` after the pull request is merged. The workflow appends `SENTRY_RELEASE=checkmo-backend@<github sha>` automatically.
4. Trigger one controlled unexpected 500 in a non-prod environment.
5. Confirm exactly one Sentry issue/event is created.
6. Inspect the event and confirm it does not contain authorization headers, cookies, JWTs, refresh tokens, passwords, verification codes, request bodies, or user context.
7. Trigger expected 400/401/403 cases and confirm no new Sentry issue is created.
8. Save dashboard evidence under `.omo/evidence/sentry-222/nonprod-sentry-event.*` with sensitive values redacted.

## Prod Rollout

Proceed only after non-prod verification passes.

1. Add the production DSN to GitHub `ENV_FILE`:

   ```text
   SENTRY_ENABLED=true
   SENTRY_DSN=<prod DSN>
   SENTRY_ENVIRONMENT=prod
   ```

2. Deploy during a low-risk window. The workflow appends `SENTRY_RELEASE=checkmo-backend@<github sha>` automatically.
3. Watch `/health`, application logs, and Sentry issue volume.
4. Confirm the GitHub Actions Sentry release step succeeds after the EC2 deploy step.
5. In Sentry, open the `checkmo-spring-boot` project and confirm release `checkmo-backend@<github sha>` appears with a `prod` deploy.
6. Confirm expected 400/401/403 traffic does not create issue noise.
7. Confirm unexpected 500 events are grouped and actionable.

## Rollback

Sentry can be disabled without code changes.

1. Set this in GitHub `ENV_FILE`:

   ```text
   SENTRY_ENABLED=false
   ```

2. Redeploy through the existing GitHub Actions workflow, or restart the EC2 stack:

   ```bash
   cd /home/ubuntu/app
   docker compose up -d
   ```

3. Confirm `/health` is still healthy.
4. If the code itself must be reverted, revert the commits for issue #222 and redeploy.

Release/deploy automation can be rolled back independently of runtime event sending:
- Revert the workflow change that adds `getsentry/action-release@v3`.
- Remove or rotate GitHub `SENTRY_AUTH_TOKEN`.
- Revoke the Sentry Internal Integration token if it is no longer needed.
