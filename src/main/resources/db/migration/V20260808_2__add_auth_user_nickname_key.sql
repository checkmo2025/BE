ALTER TABLE auth_user
    ADD COLUMN nick_name_key VARCHAR(20)
        CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL;

-- V20260808_1에서 두 테이블의 기존 충돌을 먼저 확인했다.
UPDATE auth_user
SET nick_name_key = LOWER(nick_name)
WHERE nick_name IS NOT NULL
  AND nick_name <> '';

ALTER TABLE auth_user
    ADD CONSTRAINT UK_auth_user_nickname_key UNIQUE (nick_name_key);

-- 표시용 닉네임이 아니라 NFC + 소문자 비교 키만 유일성 기준으로 사용한다.
ALTER TABLE auth_user
    DROP INDEX UK_auth_user_nickname;
