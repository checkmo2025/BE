CREATE TABLE IF NOT EXISTS report (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reporter_id VARCHAR(255) NOT NULL,
    report_target_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(255) NOT NULL,
    report_reason VARCHAR(50) NOT NULL,
    content VARCHAR(500),
    redirect_url VARCHAR(500) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
    ) ENGINE=InnoDB;

INSERT INTO report (
    reporter_id,
    report_target_type,
    target_id,
    report_reason,
    content,
    redirect_url,
    created_at,
    updated_at
)
SELECT
    mr.reporter_id,
    'MEMBER' AS target_type,
    reported.nick_name AS target_id,
    'GENERAL' AS reason,
    mr.content,
    CONCAT('/api/members/', reported.nick_name) AS redirect_url,
    mr.created_at,
    mr.updated_at
FROM member_report mr
         JOIN member reported
              ON mr.reported_member_id = reported.id;

DROP TABLE member_report;