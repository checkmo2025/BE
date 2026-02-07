SET @now = NOW();

-- =========================
-- club (3~12)
-- =========================
INSERT INTO club (id, name, description, profile_img_url, is_open, region, last_activity_at, created_at, updated_at)
VALUES
    (3,  '한강 낭독회',            '한강 작가 작품을 함께 읽고 이야기해요',         NULL, b'1', '서울',  DATE_SUB(@now, INTERVAL 3 DAY),  @now, @now),
    (4,  '부산 과학책 클럽',       '과학/교양서를 쉽게 풀어 이야기합니다',           NULL, b'1', '부산',  DATE_SUB(@now, INTERVAL 10 DAY), @now, @now),
    (5,  '인문학 산책 모임',        '인문 고전을 읽고 산책하며 토론해요',            NULL, b'1', '경기',  DATE_SUB(@now, INTERVAL 34 DAY), @now, @now),
    (6,  '온라인 에세이 스터디',     '매주 에세이 1편 읽고 글쓰기 과제도 해요',       NULL, b'1', '온라인', DATE_SUB(@now, INTERVAL 1 DAY),  @now, @now),
    (7,  '자기계발 루틴메이커',      '자기계발/습관/목표관리 책 위주로 진행합니다',    NULL, b'0', '대구',  DATE_SUB(@now, INTERVAL 90 DAY), @now, @now),
    (8,  'IT 북클럽',              '개발/IT 도서 함께 읽고 실습까지',                NULL, b'1', '서울',  DATE_SUB(@now, INTERVAL 7 DAY),  @now, @now),
    (9,  '역사 문화 읽기',          '역사/문화 책을 중심으로 깊게 읽습니다',           NULL, b'0', '인천',  DATE_SUB(@now, INTERVAL 1 DAY), @now, @now),
    (10, '여행 기록 독서회',        '여행 에세이/기행문 읽고 서로의 경험 공유',        NULL, b'1', '제주',  DATE_SUB(@now, INTERVAL 15 DAY), @now, @now),
    (11, '외국어 원서 읽기',        '외국어 원서를 함께 읽고 표현을 정리합니다',        NULL, b'0', '서울',  DATE_SUB(@now, INTERVAL 365 DAY), @now, @now),
    (12, '예술 대중문화 클럽',      '영화/음악/미술 관련 책을 읽고 감상 나눔',         NULL, b'1', '광주',  DATE_SUB(@now, INTERVAL 5 DAY),  @now, @now);

-- =========================
-- club_interest_categories (3~12)
-- =========================
INSERT INTO club_interest_categories (club_id, category)
VALUES
    (3,  'FICTION_POETRY_DRAMA'),
    (3,  'HUMANITIES'),

    (4,  'SCIENCE'),
    (4,  'COMPUTER_IT'),

    (5,  'HUMANITIES'),
    (5,  'RELIGION_PHILOSOPHY'),

    (6,  'ESSAY'),
    (6,  'SELF_DEVELOPMENT'),

    (7,  'SELF_DEVELOPMENT'),
    (7,  'ECONOMY_MANAGEMENT'),

    (8,  'COMPUTER_IT'),
    (8,  'SCIENCE'),

    (9,  'HISTORY_CULTURE'),
    (9,  'SOCIAL_SCIENCE'),

    (10, 'TRAVEL'),
    (10, 'ESSAY'),

    (11, 'FOREIGN_LANGUAGE'),
    (11, 'HUMANITIES'),

    (12, 'ART_POP_CULTURE'),
    (12, 'ESSAY');

-- =========================
-- club_participants (3~12)
-- =========================
INSERT INTO club_participants (club_id, participant_type)
VALUES
    (3,  'OFFLINE'), (3,  'WORKER'),
    (4,  'OFFLINE'), (4,  'STUDENT'),
    (5,  'OFFLINE'), (5,  'WORKER'),
    (6,  'ONLINE'),  (6,  'WORKER'),
    (7,  'OFFLINE'), (7,  'WORKER'),
    (8,  'OFFLINE'), (8,  'STUDENT'), (8, 'WORKER'),
    (9,  'OFFLINE'), (9,  'STUDENT'),
    (10, 'OFFLINE'), (10, 'MEETING'),
    (11, 'ONLINE'),  (11, 'STUDENT'),
    (12, 'OFFLINE'), (12, 'WORKER');
