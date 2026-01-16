-- 1. member 테이블 필드 추가 및 수정
ALTER TABLE member
    ADD COLUMN name VARCHAR(10) NOT NULL,
    ADD COLUMN phone_number VARCHAR(255) NOT NULL,
    MODIFY COLUMN nick_name VARCHAR(20) NOT NULL,
    MODIFY COLUMN description VARCHAR(40);

-- 2. terms (약관 마스터) 테이블 생성
CREATE TABLE IF NOT EXISTS terms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    term_url VARCHAR(255) NOT NULL,
    is_required BIT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- 3. member_terms (회원-약관 동의 내역) 테이블 생성
CREATE TABLE IF NOT EXISTS member_terms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id VARCHAR(255),
    terms_id BIGINT,
    is_agreed BIT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- 4. 외래키 제약 조건 설정 (Member와 Terms 참조)
ALTER TABLE member_terms
    ADD CONSTRAINT FK_member_terms_member
        FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE member_terms
    ADD CONSTRAINT FK_member_terms_terms
        FOREIGN KEY (terms_id) REFERENCES terms (id);

-- 5. (선택 사항) 아이디 찾기 기능을 위한 인덱스 추가
CREATE INDEX idx_member_finding_id ON member(name, phone_number);