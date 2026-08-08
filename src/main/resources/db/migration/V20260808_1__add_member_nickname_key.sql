ALTER TABLE member
    ADD COLUMN nick_name_key VARCHAR(20) NULL;

UPDATE member
SET nick_name_key = LOWER(nick_name)
WHERE nick_name IS NOT NULL;

ALTER TABLE member
    ADD CONSTRAINT UK_member_nickname_key UNIQUE (nick_name_key);
