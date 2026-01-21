ALTER TABLE club_member_vote
    ADD COLUMN created_at DATETIME(6) NULL,
    ADD COLUMN updated_at DATETIME(6) NULL;

UPDATE club_member_vote
SET created_at = COALESCE(created_at, NOW(6)),
    updated_at = COALESCE(updated_at, NOW(6));
-- BaseEntity 설정을 하지 않아서 생성/수정 일자 추적 불가 -> 일단 기존 데이터는 현재 시간으로 채움

ALTER TABLE club_member_vote
    MODIFY COLUMN created_at DATETIME(6) NOT NULL,
    MODIFY COLUMN updated_at DATETIME(6) NOT NULL;
