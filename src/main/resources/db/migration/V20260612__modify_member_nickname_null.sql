ALTER TABLE member
    MODIFY COLUMN nick_name VARCHAR(20) NULL;

UPDATE auth_user
SET nick_name = NULL
WHERE nick_name = '';

UPDATE member
SET nick_name = NULL
WHERE nick_name = '';
