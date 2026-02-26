ALTER TABLE member
    ADD COLUMN deactivated_at DATETIME(6) NULL;

CREATE INDEX idx_member_deactivated_at ON member (deactivated_at);
