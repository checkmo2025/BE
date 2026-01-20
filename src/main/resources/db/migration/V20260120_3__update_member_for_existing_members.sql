UPDATE member SET name = '이름', phone_number = '010-1234-5678' WHERE name IS NULL;

ALTER TABLE member
    MODIFY COLUMN name VARCHAR(10) NOT NULL,
    MODIFY COLUMN phone_number VARCHAR(255) NOT NULL;