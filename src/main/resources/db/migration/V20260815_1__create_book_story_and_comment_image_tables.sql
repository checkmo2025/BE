CREATE TABLE IF NOT EXISTS book_story_image (
    id BIGINT NOT NULL AUTO_INCREMENT,
    book_story_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_book_story_image_book_story
        FOREIGN KEY (book_story_id) REFERENCES book_story(id) ON DELETE CASCADE,
    UNIQUE KEY uk_book_story_image_order (book_story_id, sort_order)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS book_story_comment_image (
    id BIGINT NOT NULL AUTO_INCREMENT,
    comment_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_book_story_comment_image_comment
        FOREIGN KEY (comment_id) REFERENCES comment(id) ON DELETE CASCADE,
    UNIQUE KEY uk_book_story_comment_image_order (comment_id, sort_order)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notice_comment_image (
    id BIGINT NOT NULL AUTO_INCREMENT,
    notice_comment_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_notice_comment_image_notice_comment
        FOREIGN KEY (notice_comment_id) REFERENCES notice_comment(id) ON DELETE CASCADE,
    UNIQUE KEY uk_notice_comment_image_order (notice_comment_id, sort_order)
) ENGINE=InnoDB;

ALTER TABLE event_publication
    MODIFY COLUMN serialized_event TEXT;
