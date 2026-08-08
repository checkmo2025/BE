ALTER TABLE auth_user
    ADD COLUMN nick_name_key VARCHAR(20) NULL;

UPDATE auth_user
SET nick_name_key = LOWER(nick_name)
WHERE nick_name IS NOT NULL;

ALTER TABLE auth_user
    ADD CONSTRAINT UK_auth_user_nickname_key UNIQUE (nick_name_key);
