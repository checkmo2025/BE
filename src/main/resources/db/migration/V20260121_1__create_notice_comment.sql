-- notice_comment 테이블 생성
CREATE TABLE IF NOT EXISTS notice_comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    notice_id BIGINT NOT NULL,
    club_member_id BIGINT,
    content VARCHAR(300) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_notice_comment_notice
    FOREIGN KEY (notice_id) REFERENCES notice (id)
    ON DELETE CASCADE
) ENGINE=InnoDB;
