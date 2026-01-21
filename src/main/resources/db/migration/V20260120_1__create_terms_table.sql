
-- 1. terms (약관) 테이블 생성
CREATE TABLE IF NOT EXISTS terms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    term_url VARCHAR(255) NOT NULL,
    is_required BIT NOT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB;

-- 2. member_terms (회원-약관 동의 내역) 테이블 생성
CREATE TABLE IF NOT EXISTS member_terms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id VARCHAR(255),
    terms_id BIGINT,
    is_agreed BIT NOT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB;

