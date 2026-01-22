CREATE TABLE IF NOT EXISTS news (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(40) NOT NULL,
    requester_email VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    thumbnail_url VARCHAR(500),
    original_link VARCHAR(500),
    publish_start_at DATE NOT NULL,
    publish_end_at DATE NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS news_image (
    id BIGINT NOT NULL AUTO_INCREMENT,
    news_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_news_image_news FOREIGN KEY (news_id) REFERENCES news(id),
    UNIQUE KEY uk_news_image_order (news_id, sort_order)
);
