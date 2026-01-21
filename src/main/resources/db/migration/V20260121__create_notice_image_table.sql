CREATE TABLE IF NOT EXISTS notice_image (
    id BIGINT NOT NULL AUTO_INCREMENT,
    notice_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_notice_image_notice FOREIGN KEY (notice_id) REFERENCES notice(id),
    UNIQUE KEY uk_notice_image_order (notice_id, sort_order)
);
