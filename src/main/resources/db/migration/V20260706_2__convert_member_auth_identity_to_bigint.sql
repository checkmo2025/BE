-- Convert provider-shaped member/auth identities to a shared BIGINT identity.
-- This migration intentionally keeps member_identity_map for rollback diagnostics:
-- it records the old member/auth ids and the deterministic numeric id assigned
-- from the old member.id ordering.

CREATE TEMPORARY TABLE identity_migration_failures (
    check_name VARCHAR(128) NOT NULL,
    legacy_id VARCHAR(255),
    detail VARCHAR(500)
) ENGINE=InnoDB;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'auth_without_member', au.id, au.email
FROM auth_user au
LEFT JOIN member m ON m.id = au.id
WHERE m.id IS NULL;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'member_without_auth', m.id, m.email
FROM member m
LEFT JOIN auth_user au ON au.id = m.id
WHERE au.id IS NULL;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'invalid_auth_legacy_id', au.id, 'expected LOCAL, GOOGLE, APPLE, KAKAO, or NAVER provider prefix, underscore, and provider user id'
FROM auth_user au
WHERE LOCATE('_', au.id) <= 1
   OR LOCATE('_', au.id) = CHAR_LENGTH(au.id)
   OR UPPER(SUBSTRING_INDEX(au.id, '_', 1)) NOT IN ('LOCAL', 'GOOGLE', 'APPLE', 'KAKAO', 'NAVER');

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'duplicate_provider_identity', MIN(parsed.legacy_id),
       CONCAT(parsed.provider, ':', parsed.provider_user_id, ':', COUNT(*))
FROM (
    SELECT
        id AS legacy_id,
        UPPER(SUBSTRING_INDEX(id, '_', 1)) AS provider,
        SUBSTRING(id, LOCATE('_', id) + 1) AS provider_user_id
    FROM auth_user
    WHERE LOCATE('_', id) > 1
      AND LOCATE('_', id) < CHAR_LENGTH(id)
) parsed
GROUP BY parsed.provider, parsed.provider_user_id
HAVING COUNT(*) > 1;

CREATE TEMPORARY TABLE notification_sender_identity_candidates (
    sender_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (sender_id)
) ENGINE=InnoDB;

INSERT INTO notification_sender_identity_candidates (sender_id)
SELECT DISTINCT sender_id
FROM notification
WHERE sender_id IS NOT NULL
  AND sender_id <> 'SYSTEM';

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'member_interest_categories.member_id_orphan', r.member_id, NULL
FROM member_interest_categories r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'book_review.member_id_orphan', r.member_id, NULL
FROM book_review r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'book_story.member_id_orphan', r.member_id, NULL
FROM book_story r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'book_story_liked.member_id_orphan', r.member_id, NULL
FROM book_story_liked r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'club_member.member_id_orphan', r.member_id, NULL
FROM club_member r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'comment.member_id_orphan', r.member_id, NULL
FROM comment r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'follow.follower_id_orphan', r.follower_id, NULL
FROM follow r
LEFT JOIN member m ON m.id = r.follower_id
WHERE r.follower_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'follow.following_id_orphan', r.following_id, NULL
FROM follow r
LEFT JOIN member m ON m.id = r.following_id
WHERE r.following_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'notification.receiver_id_orphan', r.receiver_id, NULL
FROM notification r
LEFT JOIN member m ON m.id = r.receiver_id
WHERE r.receiver_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'notification.sender_id_orphan', r.sender_id, NULL
FROM notification_sender_identity_candidates r
LEFT JOIN member m ON m.id = r.sender_id
WHERE m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'topic.member_id_orphan', r.member_id, NULL
FROM topic r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'notification_setting.member_id_orphan', r.member_id, NULL
FROM notification_setting r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'member_terms.member_id_orphan', r.member_id, NULL
FROM member_terms r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'book_liked.member_id_orphan', r.member_id, NULL
FROM book_liked r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'member_block.blocker_id_orphan', r.blocker_id, NULL
FROM member_block r
LEFT JOIN member m ON m.id = r.blocker_id
WHERE r.blocker_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'member_block.blocked_id_orphan', r.blocked_id, NULL
FROM member_block r
LEFT JOIN member m ON m.id = r.blocked_id
WHERE r.blocked_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'report.reporter_id_orphan', r.reporter_id, NULL
FROM report r
LEFT JOIN member m ON m.id = r.reporter_id
WHERE r.reporter_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'team_chat_message.sender_member_id_orphan', r.sender_member_id, NULL
FROM team_chat_message r
LEFT JOIN member m ON m.id = r.sender_member_id
WHERE r.sender_member_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'push_device.member_id_orphan', r.member_id, NULL
FROM push_device r
LEFT JOIN member m ON m.id = r.member_id
WHERE r.member_id IS NOT NULL AND m.id IS NULL
LIMIT 1;

CREATE TEMPORARY TABLE identity_migration_guard (
    id INT NOT NULL,
    check_name VARCHAR(128) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

INSERT INTO identity_migration_guard (id, check_name)
VALUES (1, 'no_precondition_failures');

-- If any precondition row exists, this duplicate-key insert aborts Flyway.
INSERT INTO identity_migration_guard (id, check_name)
SELECT 1, check_name
FROM identity_migration_failures
LIMIT 1;

CREATE TABLE member_identity_map (
    old_member_identity VARCHAR(255) NOT NULL,
    old_auth_user_id VARCHAR(255) NOT NULL,
    new_member_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (old_member_identity),
    CONSTRAINT UK_member_identity_map_old_auth_user UNIQUE (old_auth_user_id),
    CONSTRAINT UK_member_identity_map_new_member UNIQUE (new_member_id),
    CONSTRAINT UK_member_identity_map_provider_user UNIQUE (provider, provider_user_id)
) ENGINE=InnoDB;

INSERT INTO member_identity_map (
    old_member_identity,
    old_auth_user_id,
    new_member_id,
    provider,
    provider_user_id
)
SELECT
    m.id,
    au.id,
    ROW_NUMBER() OVER (ORDER BY m.id),
    UPPER(SUBSTRING_INDEX(au.id, '_', 1)),
    SUBSTRING(au.id, LOCATE('_', au.id) + 1)
FROM member m
JOIN auth_user au ON au.id = m.id
ORDER BY m.id;

ALTER TABLE follow DROP FOREIGN KEY FK_follow_follower;
ALTER TABLE follow DROP FOREIGN KEY FK_follow_following;
ALTER TABLE member_interest_categories DROP FOREIGN KEY FK_member_interest_categories_member;
ALTER TABLE member_terms DROP FOREIGN KEY FK_member_terms_member;
ALTER TABLE member_block DROP FOREIGN KEY FK_member_block_blocker;
ALTER TABLE member_block DROP FOREIGN KEY FK_member_block_blocked;

ALTER TABLE member_interest_categories DROP PRIMARY KEY;
ALTER TABLE `book_story_liked` DROP INDEX UK_book_story_liked_member_book_story;
ALTER TABLE follow DROP INDEX UK_follow_follower_following;
ALTER TABLE notification_setting DROP INDEX UK_notification_setting_member;
ALTER TABLE `book_liked` DROP INDEX uk_book_liked_member_book;
ALTER TABLE member_block DROP INDEX UK_member_block_blocker_blocked;
ALTER TABLE member_block DROP INDEX IDX_member_block_blocked_id;
DROP INDEX idx_push_device_member_id ON push_device;

ALTER TABLE auth_user
    ADD COLUMN legacy_id VARCHAR(255) NULL,
    ADD COLUMN provider VARCHAR(50) NULL,
    ADD COLUMN provider_user_id VARCHAR(255) NULL,
    ADD COLUMN new_id BIGINT NULL;

ALTER TABLE member
    ADD COLUMN legacy_id VARCHAR(255) NULL,
    ADD COLUMN new_id BIGINT NULL;

UPDATE auth_user au
JOIN member_identity_map mim ON mim.old_auth_user_id = au.id
SET au.legacy_id = au.id,
    au.provider = mim.provider,
    au.provider_user_id = mim.provider_user_id,
    au.new_id = mim.new_member_id;

UPDATE member m
JOIN member_identity_map mim ON mim.old_member_identity = m.id
SET m.legacy_id = m.id,
    m.new_id = mim.new_member_id;

UPDATE member_interest_categories r
JOIN member_identity_map mim ON mim.old_member_identity = r.member_id
SET r.member_id = CAST(mim.new_member_id AS CHAR);

UPDATE `book_review` r
JOIN member_identity_map mim ON mim.old_member_identity = r.member_id
SET r.member_id = CAST(mim.new_member_id AS CHAR);

UPDATE `book_story` r
JOIN member_identity_map mim ON mim.old_member_identity = r.member_id
SET r.member_id = CAST(mim.new_member_id AS CHAR)
WHERE r.member_id IS NOT NULL;

UPDATE `book_story_liked` r
JOIN member_identity_map mim ON mim.old_member_identity = r.member_id
SET r.member_id = CAST(mim.new_member_id AS CHAR);

UPDATE club_member r
JOIN member_identity_map mim ON mim.old_member_identity = r.member_id
SET r.member_id = CAST(mim.new_member_id AS CHAR);

UPDATE comment r
JOIN member_identity_map mim ON mim.old_member_identity = r.member_id
SET r.member_id = CAST(mim.new_member_id AS CHAR)
WHERE r.member_id IS NOT NULL;

UPDATE follow r
JOIN member_identity_map mim ON mim.old_member_identity = r.follower_id
SET r.follower_id = CAST(mim.new_member_id AS CHAR)
WHERE r.follower_id IS NOT NULL;

UPDATE follow r
JOIN member_identity_map mim ON mim.old_member_identity = r.following_id
SET r.following_id = CAST(mim.new_member_id AS CHAR)
WHERE r.following_id IS NOT NULL;

UPDATE notification r
JOIN member_identity_map mim ON mim.old_member_identity = r.receiver_id
SET r.receiver_id = CAST(mim.new_member_id AS CHAR);

ALTER TABLE notification MODIFY COLUMN sender_id VARCHAR(255) NULL;

UPDATE notification
SET sender_id = NULL
WHERE sender_id = 'SYSTEM';

UPDATE notification r
JOIN member_identity_map mim ON mim.old_member_identity = r.sender_id
SET r.sender_id = CAST(mim.new_member_id AS CHAR)
WHERE r.sender_id IS NOT NULL;

UPDATE topic r
JOIN member_identity_map mim ON mim.old_member_identity = r.member_id
SET r.member_id = CAST(mim.new_member_id AS CHAR);

UPDATE notification_setting r
JOIN member_identity_map mim ON mim.old_member_identity = r.member_id
SET r.member_id = CAST(mim.new_member_id AS CHAR);

UPDATE member_terms r
JOIN member_identity_map mim ON mim.old_member_identity = r.member_id
SET r.member_id = CAST(mim.new_member_id AS CHAR);

UPDATE `book_liked` r
JOIN member_identity_map mim ON mim.old_member_identity = r.member_id
SET r.member_id = CAST(mim.new_member_id AS CHAR);

UPDATE member_block r
JOIN member_identity_map mim ON mim.old_member_identity = r.blocker_id
SET r.blocker_id = CAST(mim.new_member_id AS CHAR);

UPDATE member_block r
JOIN member_identity_map mim ON mim.old_member_identity = r.blocked_id
SET r.blocked_id = CAST(mim.new_member_id AS CHAR);

UPDATE report r
JOIN member_identity_map mim ON mim.old_member_identity = r.reporter_id
SET r.reporter_id = CAST(mim.new_member_id AS CHAR);

UPDATE team_chat_message r
JOIN member_identity_map mim ON mim.old_member_identity = r.sender_member_id
SET r.sender_member_id = CAST(mim.new_member_id AS CHAR);

UPDATE push_device r
JOIN member_identity_map mim ON mim.old_member_identity = r.member_id
SET r.member_id = CAST(mim.new_member_id AS CHAR);

ALTER TABLE auth_user DROP PRIMARY KEY;
UPDATE auth_user SET id = CAST(new_id AS CHAR);
ALTER TABLE auth_user MODIFY COLUMN id BIGINT NOT NULL;
ALTER TABLE auth_user ADD PRIMARY KEY (id);
ALTER TABLE auth_user MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE auth_user
    MODIFY COLUMN legacy_id VARCHAR(255) NOT NULL,
    MODIFY COLUMN provider VARCHAR(50) NOT NULL,
    MODIFY COLUMN provider_user_id VARCHAR(255) NOT NULL,
    DROP COLUMN new_id;

ALTER TABLE member DROP PRIMARY KEY;
UPDATE member SET id = CAST(new_id AS CHAR);
ALTER TABLE member MODIFY COLUMN id BIGINT NOT NULL;
ALTER TABLE member ADD PRIMARY KEY (id);
ALTER TABLE member MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE member
    MODIFY COLUMN legacy_id VARCHAR(255) NOT NULL,
    DROP COLUMN new_id;

ALTER TABLE member_interest_categories MODIFY COLUMN member_id BIGINT NOT NULL;
ALTER TABLE `book_review` MODIFY COLUMN member_id BIGINT NOT NULL;
ALTER TABLE `book_story` MODIFY COLUMN member_id BIGINT NULL;
ALTER TABLE `book_story_liked` MODIFY COLUMN member_id BIGINT NOT NULL;
ALTER TABLE club_member MODIFY COLUMN member_id BIGINT NOT NULL;
ALTER TABLE comment MODIFY COLUMN member_id BIGINT NULL;
ALTER TABLE follow MODIFY COLUMN follower_id BIGINT NULL;
ALTER TABLE follow MODIFY COLUMN following_id BIGINT NULL;
ALTER TABLE notification MODIFY COLUMN receiver_id BIGINT NOT NULL;
ALTER TABLE notification MODIFY COLUMN sender_id BIGINT NULL;
ALTER TABLE topic MODIFY COLUMN member_id BIGINT NOT NULL;
ALTER TABLE notification_setting MODIFY COLUMN member_id BIGINT NOT NULL;
ALTER TABLE member_terms MODIFY COLUMN member_id BIGINT NOT NULL;
ALTER TABLE `book_liked` MODIFY COLUMN member_id BIGINT NOT NULL;
ALTER TABLE member_block MODIFY COLUMN blocker_id BIGINT NOT NULL;
ALTER TABLE member_block MODIFY COLUMN blocked_id BIGINT NOT NULL;
ALTER TABLE report MODIFY COLUMN reporter_id BIGINT NOT NULL;
ALTER TABLE team_chat_message MODIFY COLUMN sender_member_id BIGINT NOT NULL;
ALTER TABLE push_device MODIFY COLUMN member_id BIGINT NOT NULL;

ALTER TABLE auth_user
    ADD CONSTRAINT UK_auth_user_legacy_id UNIQUE (legacy_id),
    ADD CONSTRAINT UK_auth_user_provider_user UNIQUE (provider, provider_user_id);

ALTER TABLE member
    ADD CONSTRAINT UK_member_legacy_id UNIQUE (legacy_id);

ALTER TABLE member_interest_categories
    ADD PRIMARY KEY (member_id, category);

ALTER TABLE `book_story_liked`
    ADD CONSTRAINT UK_book_story_liked_member_book_story UNIQUE (member_id, book_story_id);

ALTER TABLE follow
    ADD CONSTRAINT UK_follow_follower_following UNIQUE (follower_id, following_id);

ALTER TABLE notification_setting
    ADD CONSTRAINT UK_notification_setting_member UNIQUE (member_id);

ALTER TABLE `book_liked`
    ADD CONSTRAINT uk_book_liked_member_book UNIQUE (member_id, book_id);

ALTER TABLE member_block
    ADD CONSTRAINT UK_member_block_blocker_blocked UNIQUE (blocker_id, blocked_id),
    ADD INDEX IDX_member_block_blocked_id (blocked_id);

CREATE INDEX idx_push_device_member_id ON push_device (member_id);

ALTER TABLE member_interest_categories
    ADD CONSTRAINT FK_member_interest_categories_member
        FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE `book_review`
    ADD CONSTRAINT FK_book_review_member
        FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE `book_story`
    ADD CONSTRAINT FK_book_story_member
        FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE `book_story_liked`
    ADD CONSTRAINT FK_book_story_liked_member
        FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE club_member
    ADD CONSTRAINT FK_club_member_member
        FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE comment
    ADD CONSTRAINT FK_comment_member
        FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE follow
    ADD CONSTRAINT FK_follow_follower
        FOREIGN KEY (follower_id) REFERENCES member (id),
    ADD CONSTRAINT FK_follow_following
        FOREIGN KEY (following_id) REFERENCES member (id);

ALTER TABLE notification
    ADD CONSTRAINT FK_notification_receiver
        FOREIGN KEY (receiver_id) REFERENCES member (id),
    ADD CONSTRAINT FK_notification_sender
        FOREIGN KEY (sender_id) REFERENCES member (id);

ALTER TABLE topic
    ADD CONSTRAINT FK_topic_member
        FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE notification_setting
    ADD CONSTRAINT FK_notification_setting_member
        FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE member_terms
    ADD CONSTRAINT FK_member_terms_member
        FOREIGN KEY (member_id) REFERENCES member (id)
            ON DELETE CASCADE;

ALTER TABLE `book_liked`
    ADD CONSTRAINT FK_book_liked_member
        FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE member_block
    ADD CONSTRAINT FK_member_block_blocker
        FOREIGN KEY (blocker_id) REFERENCES member (id),
    ADD CONSTRAINT FK_member_block_blocked
        FOREIGN KEY (blocked_id) REFERENCES member (id);

ALTER TABLE report
    ADD CONSTRAINT FK_report_reporter
        FOREIGN KEY (reporter_id) REFERENCES member (id);

ALTER TABLE team_chat_message
    ADD CONSTRAINT FK_team_chat_message_sender_member
        FOREIGN KEY (sender_member_id) REFERENCES member (id);

ALTER TABLE push_device
    ADD CONSTRAINT FK_push_device_member
        FOREIGN KEY (member_id) REFERENCES member (id);

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'post_auth_member_id_mismatch', CAST(au.id AS CHAR), CAST(m.id AS CHAR)
FROM auth_user au
LEFT JOIN member m ON m.id = au.id
WHERE m.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_failures (check_name, legacy_id, detail)
SELECT 'post_member_auth_id_mismatch', CAST(m.id AS CHAR), CAST(au.id AS CHAR)
FROM member m
LEFT JOIN auth_user au ON au.id = m.id
WHERE au.id IS NULL
LIMIT 1;

INSERT INTO identity_migration_guard (id, check_name)
SELECT 1, check_name
FROM identity_migration_failures
WHERE check_name LIKE 'post_%'
LIMIT 1;

DROP TABLE identity_migration_guard;
DROP TABLE identity_migration_failures;
