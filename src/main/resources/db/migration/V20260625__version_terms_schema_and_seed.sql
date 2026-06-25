ALTER TABLE terms
    ADD COLUMN terms_type VARCHAR(50) NULL;

ALTER TABLE terms
    ADD COLUMN title VARCHAR(255) NULL;

ALTER TABLE terms
    ADD COLUMN version INT NULL;

ALTER TABLE terms
    ADD COLUMN is_active BIT NULL;

ALTER TABLE terms
    ADD COLUMN created_at DATETIME(6) NULL;

ALTER TABLE terms
    ADD COLUMN updated_at DATETIME(6) NULL;

ALTER TABLE member_terms
    ADD COLUMN created_at DATETIME(6) NULL;

ALTER TABLE member_terms
    ADD COLUMN updated_at DATETIME(6) NULL;

UPDATE member_terms
SET created_at = CURRENT_TIMESTAMP(6)
WHERE created_at IS NULL;

UPDATE member_terms
SET updated_at = CURRENT_TIMESTAMP(6)
WHERE updated_at IS NULL;

INSERT INTO terms (terms_type, title, term_url, version, is_active, is_required, created_at, updated_at)
VALUES
    ('SERVICE_TERMS', '책모 이용약관 동의', 'https://www.checkmo.co.kr/support/terms/service/v1', 1, true, true, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    ('PRIVACY_COLLECTION', '서비스 이용을 위한 개인정보 수집·이용 동의', 'https://www.checkmo.co.kr/support/terms/privacy-collection/v1', 1, true, true, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    ('THIRD_PARTY_PROVISION', '개인정보 제3자 제공 동의', 'https://www.checkmo.co.kr/support/terms/third-party-provision/v1', 1, true, false, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    ('MARKETING', '마케팅 및 이벤트 정보 수신 동의', 'https://www.checkmo.co.kr/support/terms/marketing/v1', 1, true, false, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

ALTER TABLE terms
    MODIFY COLUMN terms_type VARCHAR(50) NOT NULL;

ALTER TABLE terms
    MODIFY COLUMN title VARCHAR(255) NOT NULL;

ALTER TABLE terms
    MODIFY COLUMN version INT NOT NULL;

ALTER TABLE terms
    MODIFY COLUMN is_active BIT NOT NULL;

ALTER TABLE terms
    MODIFY COLUMN created_at DATETIME(6) NOT NULL;

ALTER TABLE terms
    MODIFY COLUMN updated_at DATETIME(6) NOT NULL;

ALTER TABLE member_terms
    MODIFY COLUMN member_id VARCHAR(255) NOT NULL;

ALTER TABLE member_terms
    MODIFY COLUMN terms_id BIGINT NOT NULL;

ALTER TABLE member_terms
    MODIFY COLUMN created_at DATETIME(6) NOT NULL;

ALTER TABLE member_terms
    MODIFY COLUMN updated_at DATETIME(6) NOT NULL;

ALTER TABLE member_terms
    DROP FOREIGN KEY FK_member_terms_member;

ALTER TABLE member_terms
    DROP FOREIGN KEY FK_member_terms_terms;

ALTER TABLE terms
    ADD CONSTRAINT UK_terms_type_version UNIQUE (terms_type, version);

ALTER TABLE member_terms
    ADD CONSTRAINT FK_member_terms_member
        FOREIGN KEY (member_id) REFERENCES member (id)
            ON DELETE CASCADE;

ALTER TABLE member_terms
    ADD CONSTRAINT FK_member_terms_terms
        FOREIGN KEY (terms_id) REFERENCES terms (id)
            ON DELETE RESTRICT;
