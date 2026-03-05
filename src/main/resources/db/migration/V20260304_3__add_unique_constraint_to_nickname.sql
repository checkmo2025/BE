-- 닉네임 중복 방지 및 인덱싱을 위한 UNIQUE 제약 조건 추가
ALTER TABLE auth_user MODIFY COLUMN nick_name VARCHAR(20) NOT NULL;
ALTER TABLE auth_user
    ADD CONSTRAINT UK_auth_user_nickname UNIQUE (nick_name);