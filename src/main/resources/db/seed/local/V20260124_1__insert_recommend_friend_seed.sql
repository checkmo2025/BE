-- =========================
-- 친구 추천 API 테스트용 시드 데이터
-- =========================
SET @now := NOW(6);
SET @pw_test := '$2a$10$bq9yWRc4Es2u.ihE3pGKv.g32MzfXBERMkuDCCNlO1m4FVMrZoi6C'; -- pass123!

-- =========================
-- 1) auth_user
-- =========================
INSERT INTO auth_user (id, email, password, role, profile_completed, deactivated_at, created_at, updated_at)
VALUES
    ('mem-004', 'user4@checkmo.local', @pw_test, 'USER', b'1', NULL, @now, @now),
    ('mem-005', 'user5@checkmo.local', @pw_test, 'USER', b'1', NULL, @now, @now),
    ('mem-006', 'user6@checkmo.local', @pw_test, 'USER', b'1', NULL, @now, @now),
    ('mem-007', 'user7@checkmo.local', @pw_test, 'USER', b'1', NULL, @now, @now),
    ('mem-008', 'user8@checkmo.local', @pw_test, 'USER', b'1', NULL, @now, @now),
    ('mem-009', 'user9@checkmo.local', @pw_test, 'USER', b'1', NULL, @now, @now),
    ('mem-010', 'user10@checkmo.local', @pw_test, 'USER', b'1', NULL, @now, @now);

-- =========================
-- 2) member
-- =========================
INSERT INTO member (id, email, name, phone_number, nick_name, description, img_url, created_at, updated_at)
VALUES
    ('mem-004', 'user4@checkmo.local', '이영수', '010-4444-4444', '책벌레4', '소설과 에세이를 좋아해요', NULL, @now, @now),
    ('mem-005', 'user5@checkmo.local', '박지민', '010-5555-5555', '독서광5', 'IT와 과학에 관심있어요', NULL, @now, @now),
    ('mem-006', 'user6@checkmo.local', '최수현', '010-6666-6666', '문학러버', '인문학과 역사 좋아합니다', NULL, @now, @now),
    ('mem-007', 'user7@checkmo.local', '정민호', '010-7777-7777', '경제덕후', '경제경영 전문 독서가', NULL, @now, @now),
    ('mem-008', 'user8@checkmo.local', '강서연', '010-8888-8888', '여행작가', '여행과 에세이 위주로 읽어요', NULL, @now, @now),
    ('mem-009', 'user9@checkmo.local', '윤도현', '010-9999-9999', 'IT개발자', '컴퓨터/IT 서적 좋아해요', NULL, @now, @now),
    ('mem-010', 'user10@checkmo.local', '한소희', '010-1010-1010', '다독가10', '장르 불문 다양하게 읽어요', NULL, @now, @now);

-- =========================
-- 3) member_interest_categories
-- 다양한 관심사 조합으로 추천 테스트
-- =========================
INSERT INTO member_interest_categories (member_id, category)
VALUES
    -- mem-004: DOMESTIC, FICTION_POETRY_DRAMA, ESSAY (mem-001과 3개 겹침)
    ('mem-004', 'DOMESTIC'),
    ('mem-004', 'FICTION_POETRY_DRAMA'),
    ('mem-004', 'ESSAY'),

    -- mem-005: COMPUTER_IT, SCIENCE, SELF_DEVELOPMENT (mem-001과 0개, mem-002와 1개 겹침)
    ('mem-005', 'COMPUTER_IT'),
    ('mem-005', 'SCIENCE'),
    ('mem-005', 'SELF_DEVELOPMENT'),

    -- mem-006: HUMANITIES, HISTORY_CULTURE, DOMESTIC (mem-001과 1개, mem-002와 2개 겹침)
    ('mem-006', 'HUMANITIES'),
    ('mem-006', 'HISTORY_CULTURE'),
    ('mem-006', 'DOMESTIC'),

    -- mem-007: ECONOMY_MANAGEMENT, SELF_DEVELOPMENT, POLITICS_DIPLOMACY_DEFENSE (mem-002와 2개 겹침)
    ('mem-007', 'ECONOMY_MANAGEMENT'),
    ('mem-007', 'SELF_DEVELOPMENT'),
    ('mem-007', 'POLITICS_DIPLOMACY_DEFENSE'),

    -- mem-008: TRAVEL, ESSAY, ART_POP_CULTURE (mem-001과 1개, mem-002와 1개 겹침)
    ('mem-008', 'TRAVEL'),
    ('mem-008', 'ESSAY'),
    ('mem-008', 'ART_POP_CULTURE'),

    -- mem-009: COMPUTER_IT, SCIENCE, FOREIGN_LANGUAGE (mem-001과 0개 겹침)
    ('mem-009', 'COMPUTER_IT'),
    ('mem-009', 'SCIENCE'),
    ('mem-009', 'FOREIGN_LANGUAGE'),

    -- mem-010: DOMESTIC, FICTION_POETRY_DRAMA, HUMANITIES, TRAVEL, COMPUTER_IT (다양하게 - mem-001과 2개 겹침)
    ('mem-010', 'DOMESTIC'),
    ('mem-010', 'FICTION_POETRY_DRAMA'),
    ('mem-010', 'HUMANITIES'),
    ('mem-010', 'TRAVEL'),
    ('mem-010', 'COMPUTER_IT');

-- =========================
-- 4) follow (추천에서 제외 테스트용)
-- mem-001이 mem-004를 팔로우 -> mem-001 입장에서 mem-004는 추천에서 제외되어야 함
-- =========================
INSERT INTO follow (follower_id, following_id, created_at, updated_at)
VALUES
    ('mem-001', 'mem-004', @now, @now);

-- =========================
-- 테스트 시나리오 설명
-- =========================
-- mem-001 (관심사: DOMESTIC, FICTION_POETRY_DRAMA, ESSAY) 로 테스트 시:
--   - mem-004: 3개 겹침 but 이미 팔로우 중 -> 제외
--   - mem-006: 1개 겹침 (DOMESTIC)
--   - mem-008: 1개 겹침 (ESSAY)
--   - mem-010: 2개 겹침 (DOMESTIC, FICTION_POETRY_DRAMA)
--   예상 추천 순서: mem-010(2개) > mem-006(1개) = mem-008(1개)
--
-- mem-002 (관심사: ECONOMY_MANAGEMENT, SELF_DEVELOPMENT, HUMANITIES, TRAVEL, HISTORY_CULTURE) 로 테스트 시:
--   - mem-006: 2개 겹침 (HUMANITIES, HISTORY_CULTURE)
--   - mem-007: 2개 겹침 (ECONOMY_MANAGEMENT, SELF_DEVELOPMENT)
--   - mem-005: 1개 겹침 (SELF_DEVELOPMENT)
--   - mem-008: 1개 겹침 (TRAVEL)
--   - mem-010: 2개 겹침 (HUMANITIES, TRAVEL)
--   예상 추천 순서: mem-006, mem-007, mem-010 (각 2개) > mem-005, mem-008 (각 1개)