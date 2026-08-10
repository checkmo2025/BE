-- 유일 제약 추가 전에 기존 데이터의 대소문자 무시 중복을 검사한다.
-- 충돌이 있으면 컬럼을 추가하기 전에 PRIMARY KEY 위반으로 마이그레이션을 중단한다.
CREATE TEMPORARY TABLE tmp_nickname_key_collision_guard (
    guard_key TINYINT NOT NULL PRIMARY KEY
);

INSERT INTO tmp_nickname_key_collision_guard (guard_key)
VALUES (1);

INSERT INTO tmp_nickname_key_collision_guard (guard_key)
SELECT 1
FROM (
    SELECT CONVERT(LOWER(nick_name) USING utf8mb4) COLLATE utf8mb4_bin AS nickname_key
    FROM member
    WHERE nick_name IS NOT NULL
      AND nick_name <> ''
    GROUP BY nickname_key
    HAVING COUNT(*) > 1

    UNION ALL

    SELECT CONVERT(LOWER(nick_name) USING utf8mb4) COLLATE utf8mb4_bin AS nickname_key
    FROM auth_user
    WHERE nick_name IS NOT NULL
      AND nick_name <> ''
    GROUP BY nickname_key
    HAVING COUNT(*) > 1
) nickname_key_collisions
LIMIT 1;

DROP TEMPORARY TABLE tmp_nickname_key_collision_guard;

ALTER TABLE member
    ADD COLUMN nick_name_key VARCHAR(20)
        CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL;

-- 기존 정책은 소문자 ASCII·숫자·특수문자만 허용했으므로 기존 정상 데이터에는
-- 별도 NFC 변환이 필요하지 않다. 신규 저장부터는 애플리케이션에서 NFC를 적용한다.
UPDATE member
SET nick_name_key = LOWER(nick_name)
WHERE nick_name IS NOT NULL
  AND nick_name <> '';

ALTER TABLE member
    ADD CONSTRAINT UK_member_nickname_key UNIQUE (nick_name_key);
