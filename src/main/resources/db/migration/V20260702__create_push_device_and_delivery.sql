CREATE TABLE push_device
(
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    member_id          VARCHAR(255) NOT NULL,
    installation_id    VARCHAR(36)  NOT NULL,
    expo_push_token    VARCHAR(255) NOT NULL,
    platform           VARCHAR(20)  NOT NULL,
    app_version        VARCHAR(32)  NOT NULL,
    build_number       VARCHAR(32)  NOT NULL,
    active             BIT          NOT NULL DEFAULT 1,
    last_registered_at DATETIME(6),
    deactivated_at     DATETIME(6),
    created_at         DATETIME(6),
    updated_at         DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_push_device_installation_id UNIQUE (installation_id),
    CONSTRAINT uk_push_device_expo_push_token UNIQUE (expo_push_token)
) ENGINE = InnoDB;

CREATE INDEX idx_push_device_member_id ON push_device (member_id);

CREATE TABLE push_delivery
(
    id                    BIGINT      NOT NULL AUTO_INCREMENT,
    notification_id       BIGINT      NOT NULL,
    push_device_id        BIGINT      NOT NULL,
    status                VARCHAR(30) NOT NULL,
    expo_ticket_id        VARCHAR(100),
    attempt_count         INT         NOT NULL DEFAULT 0,
    next_attempt_at       DATETIME(6),
    processing_started_at DATETIME(6),
    last_error_code       VARCHAR(100),
    last_error_message    VARCHAR(500),
    sent_at               DATETIME(6),
    receipt_checked_at    DATETIME(6),
    delivered_at          DATETIME(6),
    created_at            DATETIME(6),
    updated_at            DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_push_delivery_notification_device UNIQUE (notification_id, push_device_id),
    CONSTRAINT fk_push_delivery_notification FOREIGN KEY (notification_id) REFERENCES notification (id),
    CONSTRAINT fk_push_delivery_push_device FOREIGN KEY (push_device_id) REFERENCES push_device (id)
) ENGINE = InnoDB;

CREATE INDEX idx_push_delivery_expo_ticket_id ON push_delivery (expo_ticket_id);
CREATE INDEX idx_push_delivery_next_attempt_at ON push_delivery (next_attempt_at);
