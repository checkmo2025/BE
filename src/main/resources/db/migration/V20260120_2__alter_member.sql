-- 1. member 테이블 필드 추가 및 수정
ALTER TABLE member
    ADD COLUMN name VARCHAR(10) NULL,
    ADD COLUMN phone_number VARCHAR(255) NULL,
    MODIFY COLUMN nick_name VARCHAR(20) NOT NULL,
    MODIFY COLUMN description VARCHAR(40);

-- 2. 외래키 제약 조건 설정 (Member와 Terms 참조)
ALTER TABLE member_terms
    ADD CONSTRAINT FK_member_terms_member
        FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE member_terms
    ADD CONSTRAINT FK_member_terms_terms
        FOREIGN KEY (terms_id) REFERENCES terms (id);