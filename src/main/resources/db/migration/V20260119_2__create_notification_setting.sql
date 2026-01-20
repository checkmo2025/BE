CREATE TABLE IF NOT EXISTS notification_setting (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id VARCHAR(255) NOT NULL,
    book_story_liked BIT NOT NULL DEFAULT 1,
    book_story_comment BIT NOT NULL DEFAULT 1,
    club_notice_created BIT NOT NULL DEFAULT 1,
    club_meeting_created BIT NOT NULL DEFAULT 1,
    new_follower BIT NOT NULL DEFAULT 1,
    join_club BIT NOT NULL DEFAULT 1,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

ALTER TABLE notification_setting
    ADD CONSTRAINT UK_notification_setting_member UNIQUE (member_id);