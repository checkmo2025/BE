CREATE TABLE IF NOT EXISTS app_version_policy (
    id BIGINT NOT NULL AUTO_INCREMENT,
    platform VARCHAR(20) NOT NULL,
    min_supported_version VARCHAR(32) NOT NULL,
    latest_version VARCHAR(32) NOT NULL,
    store_url VARCHAR(500) NOT NULL,
    is_active BIT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT UK_app_version_policy_platform UNIQUE (platform)
) ENGINE=InnoDB;

INSERT INTO app_version_policy (
    platform,
    min_supported_version,
    latest_version,
    store_url,
    is_active,
    created_at,
    updated_at
)
VALUES
    (
        'IOS',
        '1.0.2',
        '1.0.2',
        'https://apps.apple.com/app/id000000000',
        true,
        CURRENT_TIMESTAMP(6),
        CURRENT_TIMESTAMP(6)
    ),
    (
        'ANDROID',
        '1.0.2',
        '1.0.2',
        'https://play.google.com/store/apps/details?id=kr.co.checkmo.app',
        true,
        CURRENT_TIMESTAMP(6),
        CURRENT_TIMESTAMP(6)
    );
