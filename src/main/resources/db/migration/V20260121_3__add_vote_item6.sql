ALTER TABLE vote
    ADD COLUMN item6 VARCHAR(255);

ALTER TABLE club_member_vote
    ADD COLUMN item6 BIT NULL;

UPDATE club_member_vote
    SET item6 = b'0'
        WHERE item6 IS NULL;

ALTER TABLE club_member_vote
    MODIFY COLUMN item6 BIT NOT NULL;