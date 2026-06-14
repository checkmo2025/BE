# Sentry Backend Monitoring

Issue: #222
Branch: `feat/222/sentry-monitoring`

## Scope

This rollout enables first-phase Sentry monitoring for the Spring Boot backend only. It is limited to unexpected 500-class exceptions handled by `ExceptionAdvice`.

Not included in this issue:
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
SENTRY_TRACES_SAMPLE_RATE=0.0|0.1
```

Default behavior is safe for local and test runs:
- `SENTRY_ENABLED=false`
- blank `SENTRY_DSN`
- no request body capture
- no default PII
- tracing sample rate `0.0`
- Sentry logs disabled

The application binds these values through `checkmo.sentry.*` properties. Runtime event capture is enabled only when `SENTRY_ENABLED=true` and `SENTRY_DSN` is not blank. Sentry Spring Boot auto-configuration is allowed for core Spring Web tracing, while profiler, Logback appender, and WebFlux auto-configurations stay excluded.

In the current deployment flow, `.github/workflows/release.yml` creates `.env` from `secrets.ENV_FILE`, removes stale Sentry runtime values, appends the current GitHub Secrets plus `SENTRY_RELEASE=checkmo-backend@${GITHUB_SHA}`, and copies it to EC2 with `compose.yml`. Operators must maintain GitHub repository secrets for `SENTRY_ENABLED`, `SENTRY_DSN`, `SENTRY_ENVIRONMENT`, and `SENTRY_TRACES_SAMPLE_RATE`; `SENTRY_RELEASE` is deployment-generated and should not be maintained manually in the secret. Do not edit, print, or commit the local `.env` file.

## Release and Deploy Tracking

Issue #232 adds CI-side Sentry release/deploy automation for the backend project. The GitHub Actions workflow creates a Sentry release named `checkmo-backend@<github sha>` after the EC2 deployment succeeds, then records a `prod` deploy for that release.

This automation uses `getsentry/action-release` pinned to a full commit SHA and requires this GitHub Actions secret:

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

## Backend Tracing

Issue #234 enables minimal Spring Web tracing for backend endpoint timing.

Tracing is different from error monitoring:
- Error monitoring records unexpected failures.
- Tracing records a sampled subset of normal requests so Sentry can show endpoint transaction names and durations.

The production default should be:

```text
SENTRY_TRACES_SAMPLE_RATE=0.1
```

`0.1` means Sentry samples about 10% of eligible backend request transactions. Set it to `0.0` and redeploy to disable tracing without reverting code.

Noise endpoints are sampled at `0.0` by the backend sampler:
- `/health`
- `/swagger-ui`
- `/v3/api-docs`

After deployment, check Sentry:
1. Open project `checkmo-spring-boot`.
2. Open Performance, Traces, or Transactions.
3. Filter by `environment:prod`.
4. Confirm backend transactions appear with endpoint names and durations.
5. If a release filter is available, use `checkmo-backend@<github sha>`.

The first success criterion is endpoint transaction timing. DB spans may appear depending on Sentry/Spring instrumentation, but DB query ranking is not guaranteed by this rollout.

Do not enable profiling, Logback log shipping, or Apdex alerts as part of this rollout.

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
   SENTRY_TRACES_SAMPLE_RATE=0.1
   ```

3. Deploy from `develop` after the pull request is merged. The workflow appends `SENTRY_RELEASE=checkmo-backend@<github sha>` automatically.
4. Trigger one controlled unexpected 500 in a non-prod environment.
5. Confirm exactly one Sentry issue/event is created.
6. Inspect the event and confirm it does not contain authorization headers, cookies, JWTs, refresh tokens, passwords, verification codes, request bodies, or user context.
7. Trigger expected 400/401/403 cases and confirm no new Sentry issue is created.
8. Trigger several safe non-sensitive API requests and confirm transaction timing appears under Performance, Traces, or Transactions.
9. Save dashboard evidence under `.omo/evidence/sentry-234/nonprod-sentry-tracing.*` with sensitive values redacted.

## Prod Rollout

Proceed only after non-prod verification passes.

1. Add the production DSN to GitHub `ENV_FILE`:

   ```text
   SENTRY_ENABLED=true
   SENTRY_DSN=<prod DSN>
   SENTRY_ENVIRONMENT=prod
   SENTRY_TRACES_SAMPLE_RATE=0.1
   ```

2. Deploy during a low-risk window. The workflow appends `SENTRY_RELEASE=checkmo-backend@<github sha>` automatically.
3. Watch `/health`, application logs, and Sentry issue volume.
4. Confirm the GitHub Actions Sentry release step succeeds after the EC2 deploy step.
5. In Sentry, open the `checkmo-spring-boot` project and confirm release `checkmo-backend@<github sha>` appears with a `prod` deploy.
6. Confirm expected 400/401/403 traffic does not create issue noise.
7. Confirm unexpected 500 events are grouped and actionable.
8. Confirm backend endpoint transactions appear with durations in Performance, Traces, or Transactions.

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

Tracing can be disabled without disabling error monitoring:

1. Set this GitHub repository secret:

   ```text
   SENTRY_TRACES_SAMPLE_RATE=0.0
   ```

2. Redeploy through the existing GitHub Actions workflow.
3. Confirm error monitoring still works and new backend transactions stop appearing.

Release/deploy automation can be rolled back independently of runtime event sending:
- Revert the workflow change that adds `getsentry/action-release`.
- Remove or rotate GitHub `SENTRY_AUTH_TOKEN`.
- Revoke the Sentry Internal Integration token if it is no longer needed.
