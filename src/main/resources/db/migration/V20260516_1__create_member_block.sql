CREATE TABLE IF NOT EXISTS member_block (
    id BIGINT NOT NULL AUTO_INCREMENT,
    blocker_id VARCHAR(255) NOT NULL,
    blocked_id VARCHAR(255) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT UK_member_block_blocker_blocked UNIQUE (blocker_id, blocked_id),
    CONSTRAINT FK_member_block_blocker FOREIGN KEY (blocker_id) REFERENCES member (id),
    CONSTRAINT FK_member_block_blocked FOREIGN KEY (blocked_id) REFERENCES member (id),
    INDEX IDX_member_block_blocked_id (blocked_id)
) ENGINE=InnoDB;
