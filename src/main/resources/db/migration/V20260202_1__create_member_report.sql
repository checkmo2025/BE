CREATE TABLE IF NOT EXISTS member_report (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reporter_id VARCHAR(255) NOT NULL,
    reported_member_id VARCHAR(255) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    content VARCHAR(500),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

ALTER TABLE member_report
    ADD CONSTRAINT FK_member_report_reporter FOREIGN KEY (reporter_id) REFERENCES member(id);

ALTER TABLE member_report
    ADD CONSTRAINT FK_member_report_reported_member FOREIGN KEY (reported_member_id) REFERENCES member(id);