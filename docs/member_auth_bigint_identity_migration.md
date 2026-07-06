# 회원 인증 BIGINT 식별자 마이그레이션 runbook

## 목적과 불변조건

이 문서는 기존 provider-shaped 문자열 식별자(`LOCAL_*`, `GOOGLE_*`, `APPLE_*`, `KAKAO_*`)를 내부 숫자 식별자로 전환하기 위한 운영 preflight, rollout, rollback 절차이다.

- `auth_user.id`만 `BIGINT` 생성 원천이다.
- `member.id`는 `auth_user.id`와 같은 값을 할당받아 공유한다.
- target schema에 `auth_user.member_id` 또는 `member.auth_user_id`를 추가하지 않는다.
- 기존 문자열 ID는 `auth_user.legacy_id`, `member.legacy_id`에 보존한다.
- `auth_user.provider`, `auth_user.provider_user_id`, `UNIQUE(provider, provider_user_id)`를 추가한다.
- cutover 시 기존 access token과 refresh token은 무효화한다. 사용자는 재로그인해야 한다.
- PR #275 push notification 병합분의 `push_device.member_id`도 같은 전환 범위에 포함한다.

## secret handling

운영자가 이 문서로 작업할 때도 secret-bearing 값을 읽거나 기록하지 않는다.

- `.env`, AWS credentials, RDS_PASSWORD, AWS_ACCESS_KEY, AWS_SECRET, JWT_SECRET, OAuth secret, GitHub secret 값을 읽지 않는다.
- 터미널, 문서, PR, 이슈, 로그, evidence 파일에 credential, endpoint URL, host name, token 값을 붙여 넣지 않는다.
- DB 접속은 운영자가 이미 승인된 secret manager 또는 사내 접속 절차로 수행하고, 이 runbook에는 접속 문자열을 기록하지 않는다.
- preflight와 smoke output은 aggregate/count 또는 필요한 진단 컬럼만 남긴다. 인증 secret 컬럼 값은 출력하지 않는다.

## 사전 운영 조건

- maintenance window를 공지하고 쓰기 트래픽을 중지할 수 있는 시간을 확보한다.
- 마이그레이션과 애플리케이션 코드는 같은 cutover 배포 단위로 준비한다.
- 운영 반영 전 local MySQL 또는 staging-equivalent DB에서 Flyway dry-run을 완료한다.
- RDS snapshot 또는 동등한 point-in-time restore 가능한 backup을 생성한다.
- rollback은 migration 역실행이 아니라 snapshot restore 기준으로 수행한다.
- preflight query 결과가 모두 0건 또는 기대 count 일치일 때만 진행한다.

## Preflight SQL

아래 SQL은 기존 문자열 식별자 schema에서 실행한다. 결과 count가 0이 아니면 cutover를 중단하고 데이터를 먼저 정리한다.

### auth/member one-to-one mismatch

```sql
SELECT
    'auth_without_member' AS check_name,
    COUNT(*) AS mismatch_count
FROM auth_user au
LEFT JOIN member m ON m.id = au.id
WHERE m.id IS NULL;

SELECT
    'member_without_auth' AS check_name,
    COUNT(*) AS mismatch_count
FROM member m
LEFT JOIN auth_user au ON au.id = m.id
WHERE au.id IS NULL;
```

진단이 필요할 때만 제한된 컬럼으로 샘플을 확인한다.

```sql
SELECT
    au.id AS auth_legacy_id,
    au.email AS auth_email
FROM auth_user au
LEFT JOIN member m ON m.id = au.id
WHERE m.id IS NULL
ORDER BY au.id
LIMIT 50;

SELECT
    m.id AS member_legacy_id,
    m.email AS member_email,
    m.nick_name AS member_nickname
FROM member m
LEFT JOIN auth_user au ON au.id = m.id
WHERE au.id IS NULL
ORDER BY m.id
LIMIT 50;
```

### orphan member reference checks

모든 member reference column은 cutover 전에 `member.id`에 매칭되어야 한다. nullable column은 `NULL`을 orphan으로 보지 않는다. `notification.sender_id = 'SYSTEM'`은 시스템 발신자를 뜻하므로 preflight orphan으로 보지 않고, cutover 후 `NULL`로 변환한다.

```sql
SELECT 'member_interest_categories.member_id' AS reference_column, COUNT(*) AS orphan_count
FROM member_interest_categories r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'book_review.member_id', COUNT(*)
FROM book_review r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'book_story.member_id', COUNT(*)
FROM book_story r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'book_story_liked.member_id', COUNT(*)
FROM book_story_liked r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'club_member.member_id', COUNT(*)
FROM club_member r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'comment.member_id', COUNT(*)
FROM comment r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'follow.follower_id', COUNT(*)
FROM follow r
LEFT JOIN member m ON m.id = r.follower_id
WHERE r.follower_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'follow.following_id', COUNT(*)
FROM follow r
LEFT JOIN member m ON m.id = r.following_id
WHERE r.following_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'notification.receiver_id', COUNT(*)
FROM notification r
LEFT JOIN member m ON m.id = r.receiver_id
WHERE r.receiver_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'notification.sender_id', COUNT(*)
FROM notification r
LEFT JOIN member m ON m.id = r.sender_id
WHERE r.sender_id IS NOT NULL AND r.sender_id <> 'SYSTEM' AND m.id IS NULL
UNION ALL
SELECT 'topic.member_id', COUNT(*)
FROM topic r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'notification_setting.member_id', COUNT(*)
FROM notification_setting r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'member_terms.member_id', COUNT(*)
FROM member_terms r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'book_liked.member_id', COUNT(*)
FROM book_liked r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'member_block.blocker_id', COUNT(*)
FROM member_block r
LEFT JOIN member m ON m.id = r.blocker_id
WHERE r.blocker_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'member_block.blocked_id', COUNT(*)
FROM member_block r
LEFT JOIN member m ON m.id = r.blocked_id
WHERE r.blocked_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'report.reporter_id', COUNT(*)
FROM report r
LEFT JOIN member m ON m.id = r.reporter_id
WHERE r.reporter_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'team_chat_message.sender_member_id', COUNT(*)
FROM team_chat_message r
LEFT JOIN member m ON m.id = r.sender_member_id
WHERE r.sender_member_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'push_device.member_id', COUNT(*)
FROM push_device r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL;
```

orphan 샘플이 필요하면 아래 패턴을 column별로 실행한다.

```sql
SELECT
    r.id AS row_id,
    r.member_id AS legacy_member_id
FROM push_device r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
ORDER BY r.id
LIMIT 50;
```

### provider parse checks

`provider_user_id`는 첫 번째 `_` 뒤의 나머지 문자열이다. 같은 provider와 provider user id 조합이 2건 이상이면 `UNIQUE(provider, provider_user_id)` 생성 전에 중단한다.

```sql
WITH parsed_auth AS (
    SELECT
        id AS legacy_id,
        SUBSTRING_INDEX(id, '_', 1) AS provider,
        SUBSTRING(id, LOCATE('_', id) + 1) AS provider_user_id
    FROM auth_user
    WHERE id LIKE 'LOCAL\_%'
       OR id LIKE 'GOOGLE\_%'
       OR id LIKE 'APPLE\_%'
       OR id LIKE 'KAKAO\_%'
)
SELECT
    provider,
    provider_user_id,
    COUNT(*) AS duplicate_count
FROM parsed_auth
GROUP BY provider, provider_user_id
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC, provider, provider_user_id
LIMIT 100;
```

provider별로 별도 검증이 필요하면 아래 SQL을 provider 값만 바꿔 반복한다.

```sql
SELECT
    'LOCAL' AS provider,
    SUBSTRING(id, LOCATE('_', id) + 1) AS provider_user_id,
    COUNT(*) AS duplicate_count
FROM auth_user
WHERE id LIKE 'LOCAL\_%'
GROUP BY SUBSTRING(id, LOCATE('_', id) + 1)
HAVING COUNT(*) > 1
UNION ALL
SELECT
    'GOOGLE' AS provider,
    SUBSTRING(id, LOCATE('_', id) + 1) AS provider_user_id,
    COUNT(*) AS duplicate_count
FROM auth_user
WHERE id LIKE 'GOOGLE\_%'
GROUP BY SUBSTRING(id, LOCATE('_', id) + 1)
HAVING COUNT(*) > 1
UNION ALL
SELECT
    'APPLE' AS provider,
    SUBSTRING(id, LOCATE('_', id) + 1) AS provider_user_id,
    COUNT(*) AS duplicate_count
FROM auth_user
WHERE id LIKE 'APPLE\_%'
GROUP BY SUBSTRING(id, LOCATE('_', id) + 1)
HAVING COUNT(*) > 1
UNION ALL
SELECT
    'KAKAO' AS provider,
    SUBSTRING(id, LOCATE('_', id) + 1) AS provider_user_id,
    COUNT(*) AS duplicate_count
FROM auth_user
WHERE id LIKE 'KAKAO\_%'
GROUP BY SUBSTRING(id, LOCATE('_', id) + 1)
HAVING COUNT(*) > 1;
```

예상하지 않은 prefix 또는 `_`가 없는 legacy id는 중단 조건이다.

```sql
SELECT
    CASE
        WHEN LOCATE('_', id) = 0 THEN '(missing_separator)'
        ELSE SUBSTRING_INDEX(id, '_', 1)
    END AS legacy_prefix,
    COUNT(*) AS row_count
FROM auth_user
WHERE LOCATE('_', id) = 0
   OR SUBSTRING_INDEX(id, '_', 1) NOT IN ('LOCAL', 'GOOGLE', 'APPLE', 'KAKAO')
GROUP BY legacy_prefix
ORDER BY row_count DESC, legacy_prefix;
```

### row-count baseline

마이그레이션 전후에 같은 table count를 비교한다. 운영에서는 결과를 안전한 작업 로그에 저장하되 credential과 endpoint는 포함하지 않는다.

```sql
SELECT 'auth_user' AS table_name, COUNT(*) AS row_count FROM auth_user
UNION ALL SELECT 'member', COUNT(*) FROM member
UNION ALL SELECT 'member_interest_categories', COUNT(*) FROM member_interest_categories
UNION ALL SELECT 'book_review', COUNT(*) FROM book_review
UNION ALL SELECT 'book_story', COUNT(*) FROM book_story
UNION ALL SELECT 'book_story_liked', COUNT(*) FROM book_story_liked
UNION ALL SELECT 'club_member', COUNT(*) FROM club_member
UNION ALL SELECT 'comment', COUNT(*) FROM comment
UNION ALL SELECT 'follow', COUNT(*) FROM follow
UNION ALL SELECT 'notification', COUNT(*) FROM notification
UNION ALL SELECT 'topic', COUNT(*) FROM topic
UNION ALL SELECT 'notification_setting', COUNT(*) FROM notification_setting
UNION ALL SELECT 'member_terms', COUNT(*) FROM member_terms
UNION ALL SELECT 'book_liked', COUNT(*) FROM book_liked
UNION ALL SELECT 'member_block', COUNT(*) FROM member_block
UNION ALL SELECT 'report', COUNT(*) FROM report
UNION ALL SELECT 'team_chat_message', COUNT(*) FROM team_chat_message
UNION ALL SELECT 'push_device', COUNT(*) FROM push_device;
```

dry-run에서 같은 DB session으로 비교할 수 있으면 임시 테이블을 사용한다.

```sql
CREATE TEMPORARY TABLE member_auth_bigint_row_counts_before AS
SELECT 'auth_user' AS table_name, COUNT(*) AS row_count FROM auth_user
UNION ALL SELECT 'member', COUNT(*) FROM member
UNION ALL SELECT 'push_device', COUNT(*) FROM push_device;

-- Flyway migration 실행 후 같은 session에서:
CREATE TEMPORARY TABLE member_auth_bigint_row_counts_after AS
SELECT 'auth_user' AS table_name, COUNT(*) AS row_count FROM auth_user
UNION ALL SELECT 'member', COUNT(*) FROM member
UNION ALL SELECT 'push_device', COUNT(*) FROM push_device;

SELECT
    b.table_name,
    b.row_count AS before_count,
    a.row_count AS after_count,
    a.row_count - b.row_count AS delta_count
FROM member_auth_bigint_row_counts_before b
JOIN member_auth_bigint_row_counts_after a ON a.table_name = b.table_name
WHERE a.row_count <> b.row_count
ORDER BY b.table_name;
```

## Rollout order

1. maintenance window 시작을 공지하고 신규 쓰기 요청과 batch/scheduler 실행을 중지한다.
2. RDS snapshot 또는 동등한 backup을 생성하고 snapshot 식별자만 내부 운영 기록에 남긴다.
3. 기존 token은 cutover 후 무효화된다고 공지한다.
4. 위 preflight SQL을 실행한다. mismatch, orphan, duplicate provider identity, unknown prefix 결과가 있으면 중단한다.
5. row-count baseline을 캡처한다.
6. 새 Flyway migration과 애플리케이션 코드를 같은 배포 단위로 반영한다.
7. migration은 `member_identity_map`을 생성해 old legacy id와 new bigint id 매핑을 고정해야 한다.
8. migration은 `auth_user.legacy_id`, `member.legacy_id`, `auth_user.provider`, `auth_user.provider_user_id`를 채우고 `UNIQUE(provider, provider_user_id)`를 생성해야 한다.
9. 모든 member reference column을 `member_identity_map`으로 `BIGINT`에 backfill한 뒤 FK와 unique index를 다시 검증한다. `notification.sender_id = 'SYSTEM'`은 nullable `BIGINT` 컬럼의 `NULL`로 변환한다.
10. refresh token 저장소는 기존 문자열 subject 기반 key를 제거하거나 만료되도록 둔다. 재로그인 후 numeric subject 기반 token만 유효해야 한다.
11. smoke test가 끝날 때까지 외부 트래픽을 재개하지 않는다.

## Post-cutover validation SQL

아래 SQL은 target schema 적용 후 실행한다.

### core identity and provider validation

```sql
SELECT
    'auth_member_shared_bigint_id' AS check_name,
    COUNT(*) AS mismatch_count
FROM auth_user au
LEFT JOIN member m ON m.id = au.id
WHERE m.id IS NULL;

SELECT
    'member_without_auth_after_cutover' AS check_name,
    COUNT(*) AS mismatch_count
FROM member m
LEFT JOIN auth_user au ON au.id = m.id
WHERE au.id IS NULL;

SELECT
    'provider_identity_duplicate_after_cutover' AS check_name,
    COUNT(*) AS duplicate_group_count
FROM (
    SELECT provider, provider_user_id
    FROM auth_user
    GROUP BY provider, provider_user_id
    HAVING COUNT(*) > 1
) duplicated_provider_identity;
```

### column type validation

```sql
SELECT
    table_name,
    column_name,
    data_type,
    column_type
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND (
      (table_name = 'auth_user' AND column_name IN ('id', 'legacy_id', 'provider', 'provider_user_id'))
      OR (table_name = 'member' AND column_name IN ('id', 'legacy_id'))
      OR (table_name = 'member_interest_categories' AND column_name = 'member_id')
      OR (table_name = 'book_review' AND column_name = 'member_id')
      OR (table_name = 'book_story' AND column_name = 'member_id')
      OR (table_name = 'book_story_liked' AND column_name = 'member_id')
      OR (table_name = 'club_member' AND column_name = 'member_id')
      OR (table_name = 'comment' AND column_name = 'member_id')
      OR (table_name = 'follow' AND column_name IN ('follower_id', 'following_id'))
      OR (table_name = 'notification' AND column_name IN ('receiver_id', 'sender_id'))
      OR (table_name = 'topic' AND column_name = 'member_id')
      OR (table_name = 'notification_setting' AND column_name = 'member_id')
      OR (table_name = 'member_terms' AND column_name = 'member_id')
      OR (table_name = 'book_liked' AND column_name = 'member_id')
      OR (table_name = 'member_block' AND column_name IN ('blocker_id', 'blocked_id'))
      OR (table_name = 'report' AND column_name = 'reporter_id')
      OR (table_name = 'team_chat_message' AND column_name = 'sender_member_id')
      OR (table_name = 'push_device' AND column_name = 'member_id')
  )
ORDER BY table_name, column_name;
```

`notification.receiver_id`는 `BIGINT NOT NULL`, `notification.sender_id`는 시스템 발신자를 표현하기 위해 `BIGINT NULL`이어야 한다.

### post-cutover FK validation

```sql
SELECT
    table_name,
    column_name,
    referenced_table_name,
    referenced_column_name,
    constraint_name
FROM information_schema.key_column_usage
WHERE table_schema = DATABASE()
  AND referenced_table_name = 'member'
  AND referenced_column_name = 'id'
  AND (
      (table_name = 'member_interest_categories' AND column_name = 'member_id')
      OR (table_name = 'book_review' AND column_name = 'member_id')
      OR (table_name = 'book_story' AND column_name = 'member_id')
      OR (table_name = 'book_story_liked' AND column_name = 'member_id')
      OR (table_name = 'club_member' AND column_name = 'member_id')
      OR (table_name = 'comment' AND column_name = 'member_id')
      OR (table_name = 'follow' AND column_name IN ('follower_id', 'following_id'))
      OR (table_name = 'notification' AND column_name IN ('receiver_id', 'sender_id'))
      OR (table_name = 'topic' AND column_name = 'member_id')
      OR (table_name = 'notification_setting' AND column_name = 'member_id')
      OR (table_name = 'member_terms' AND column_name = 'member_id')
      OR (table_name = 'book_liked' AND column_name = 'member_id')
      OR (table_name = 'member_block' AND column_name IN ('blocker_id', 'blocked_id'))
      OR (table_name = 'report' AND column_name = 'reporter_id')
      OR (table_name = 'team_chat_message' AND column_name = 'sender_member_id')
      OR (table_name = 'push_device' AND column_name = 'member_id')
  )
ORDER BY table_name, column_name;
```

FK가 있어도 변환 누락이 없는지 anti-join으로 한 번 더 확인한다.

```sql
SELECT 'push_device.member_id' AS reference_column, COUNT(*) AS orphan_count
FROM push_device r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'report.reporter_id', COUNT(*)
FROM report r
LEFT JOIN member m ON m.id = r.reporter_id
WHERE r.reporter_id IS NOT NULL AND m.id IS NULL
UNION ALL
SELECT 'team_chat_message.sender_member_id', COUNT(*)
FROM team_chat_message r
LEFT JOIN member m ON m.id = r.sender_member_id
WHERE r.sender_member_id IS NOT NULL AND m.id IS NULL;
```

## Smoke checklist

- `/health`가 정상 응답한다.
- 기존 token으로 인증 API를 호출하면 실패하고, 재로그인 후 새 token으로 성공한다.
- 로그인 후 내 프로필 조회에서 numeric member id 기반 인증 흐름이 동작한다.
- 회원 약관, follow/block, club membership, book story/comment/like, notification setting/list/read, push_device 등록/해제, report 생성/조회, team chat history 대표 API를 확인한다.
- `auth_user.legacy_id`와 `member.legacy_id`는 기존 provider-shaped 값으로 남아야 한다.
- `auth_user.provider_user_id`는 prefix를 제거한 provider 원본 id여야 한다.

## Rollback order

rollback은 smoke 실패가 외부 트래픽 재개 전에 발견된 경우에만 즉시 수행한다.

1. 외부 트래픽과 scheduler/batch를 계속 중지한다.
2. 실패 원인과 마지막 성공 단계, migration 로그, row-count output을 기록한다. secret 값은 기록하지 않는다.
3. cutover 직전 snapshot으로 DB를 restore한다.
4. 이전 애플리케이션 artifact로 되돌린다.
5. restore된 DB에서 preflight row-count baseline과 핵심 auth/member count를 비교한다.
6. `/health`와 로그인 smoke를 확인한다.
7. 문제가 없으면 트래픽을 재개하고, 기존 token 정책은 rollback된 코드 기준으로 운영한다.
8. 이미 numeric token을 발급한 짧은 구간이 있으면 해당 token은 폐기 대상으로 보고 사용자는 다시 로그인하게 한다.

## 중단 조건

- auth/member one-to-one mismatch가 1건 이상이다.
- member reference orphan이 1건 이상이다.
- provider/provider_user_id duplicate가 1건 이상이다.
- `LOCAL`, `GOOGLE`, `APPLE`, `KAKAO` 외 prefix 또는 `_` 없는 legacy id가 존재한다.
- `auth_user.id`와 `member.id`가 독립 생성될 수 있는 migration 설계가 발견된다.
- target schema에 `auth_user.member_id` 또는 `member.auth_user_id`가 추가된다.
- backup/snapshot 생성 여부가 확인되지 않는다.
- secret 값이 문서, evidence, terminal transcript에 노출된다.
