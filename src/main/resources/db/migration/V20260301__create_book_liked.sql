ALTER TABLE book
    ADD COLUMN likes INTEGER NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS book_liked (
    id BIGINT NOT NULL AUTO_INCREMENT,
    book_id VARCHAR(255) NOT NULL,
    member_id VARCHAR(255) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_book_liked_member_book UNIQUE (member_id, book_id),
    CONSTRAINT fk_book_liked_book FOREIGN KEY (book_id) REFERENCES book (id)
) ENGINE=InnoDB;
