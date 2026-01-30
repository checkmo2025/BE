-- CLUB: open -> is_open
ALTER TABLE club
    RENAME COLUMN `open` TO `is_open`;

-- CLUB_CONTACTS: ElementCollection(Embeddable) 테이블 신규 생성
CREATE TABLE IF NOT EXISTS club_contacts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    club_id BIGINT NOT NULL,
    link VARCHAR(100) NOT NULL,
    label VARCHAR(20) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_club_contacts_club
    FOREIGN KEY (club_id) REFERENCES club(id)
    );

-- club.insta, club.kakao값 -> club_contacts로 이전
INSERT IGNORE INTO club_contacts (club_id, link, label)
SELECT c.id, c.insta, 'insta'
FROM club c
WHERE c.insta IS NOT NULL
  AND TRIM(c.insta) <> '';

INSERT IGNORE INTO club_contacts (club_id, link, label)
SELECT c.id, c.kakao, 'kakao'
FROM club c
WHERE c.kakao IS NOT NULL
  AND TRIM(c.kakao) <> '';

-- club: insta, kakao 컬럼 제거
ALTER TABLE club
    DROP COLUMN insta,
    DROP COLUMN kakao;

-- club_member에 applied_at, joined_at, ended_at 컬럼 추가
ALTER TABLE club_member
    ADD COLUMN applied_at DATETIME NULL,
    ADD COLUMN joined_at DATETIME NULL,
    ADD COLUMN ended_at DATETIME NULL;

-- club_member_status 값 마이그레이션
UPDATE club_member cm
SET club_member_status = 'WITHDRAWN'
WHERE cm.club_member_status = 'BLOCKED';

-- 기존 데이터의 신청/가입/탈퇴 시각 채우기
UPDATE club_member
SET applied_at = COALESCE(applied_at, created_at)
WHERE applied_at IS NULL;

UPDATE club_member
SET joined_at = created_at
WHERE joined_at IS NULL
  AND club_member_status IN ('MEMBER', 'STAFF', 'OWNER');

UPDATE club_member
SET ended_at = updated_at
WHERE ended_at IS NULL
AND club_member_status = 'WITHDRAWN';

-- 각 club의 최초 STAFF를 OWNER로 승격
UPDATE club_member cm
    JOIN (
    SELECT x.club_id, MIN(x.id) AS min_staff_id
    FROM club_member x
    WHERE x.club_member_status = 'STAFF'
    AND NOT EXISTS (
    SELECT 1
    FROM club_member y
    WHERE y.club_id = x.club_id
    AND y.club_member_status = 'OWNER'
    )
    GROUP BY x.club_id
    ) t ON cm.id = t.min_staff_id
    SET cm.club_member_status = 'OWNER';