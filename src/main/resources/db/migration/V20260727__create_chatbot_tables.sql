CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT,
    session_token VARCHAR(64) NOT NULL,
    unresolved BOOLEAN NOT NULL DEFAULT FALSE,
    last_activity_at DATETIME(6) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_chat_session_session_token (session_token),
    KEY idx_chat_session_member_id (member_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    chat_session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    masked_content TEXT NOT NULL,
    model_used VARCHAR(100),
    escalated BOOLEAN NOT NULL DEFAULT FALSE,
    bot_uncertain BOOLEAN NOT NULL DEFAULT FALSE,
    user_negative_reaction BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    KEY idx_chat_message_chat_session_id (chat_session_id)
) ENGINE=InnoDB;
