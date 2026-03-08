-- nickname 컬럼 추가 (최대 20자, 초기에는 NULL 허용)
ALTER TABLE auth_user
    ADD COLUMN nick_name VARCHAR(20) NULL;