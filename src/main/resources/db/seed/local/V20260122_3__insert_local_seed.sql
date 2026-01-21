-- =========================
-- 0) 공통 타임스탬프
-- =========================
SET @now := NOW(6);

-- =========================
-- 1) auth_user (로그인 테스트 계정)
-- =========================
SET @pw_test  := '$2a$10$bq9yWRc4Es2u.ihE3pGKv.g32MzfXBERMkuDCCNlO1m4FVMrZoi6C'; -- pass123!

INSERT INTO auth_user (id, email, password, role, profile_completed, deactivated_at, created_at, updated_at)
VALUES
    ('mem-001', 'test1@checkmo.local', @pw_test,  'USER',  b'1', NULL, @now, @now),
    ('mem-002', 'test2@checkmo.local', @pw_test,  'USER',  b'1', NULL, @now, @now),
    ('mem-003','admin@checkmo.local', @pw_test, 'ADMIN', b'1', NULL, @now, @now);

-- =========================
-- 2) member
-- =========================
INSERT INTO member (id, email, name, phone_number, nick_name, description, img_url, created_at, updated_at)
VALUES
    ('mem-001', 'test1@checkmo.local', '홍길동', '010-1234-5678', '테스터1', '로컬 시드 유저1', NULL, @now, @now),
    ('mem-002', 'test2@checkmo.local', '김철수', '010-2345-6789', '테스터2', '로컬 시드 유저2', NULL, @now, @now),
    ('mem-003', 'admin@checkmo.local', '박영희', '010-3456-7890', '어드민1', '어드민 시드 유저1', NULL, @now, @now);

-- =========================
-- 3) member_interest_categories
-- =========================
INSERT INTO member_interest_categories (member_id, category)
VALUES
    ('mem-001', 'DOMESTIC'),
    ('mem-001', 'FICTION_POETRY_DRAMA'),
    ('mem-001', 'ESSAY'),
    ('mem-002', 'ECONOMY_MANAGEMENT'),
    ('mem-002', 'SELF_DEVELOPMENT'),
    ('mem-002', 'HUMANITIES'),
    ('mem-002', 'TRAVEL'),
    ('mem-002', 'HISTORY_CULTURE'),
    ('mem-003', 'SOCIAL_SCIENCE'),
    ('mem-003', 'CHILDREN');

-- =========================
-- 4) book
-- =========================
INSERT INTO book (id, title, author, publisher, description, img_url, created_at, updated_at)
VALUES
    ('9791192005317', '살인자ㅇ난감', '꼬마비', '글의온도',
     '주변 어디에서나 흔히 볼 수 있는 평범한 대학생인 주인공 이탕은 야간 편의점 아르바이트 중 어떤 진상 손님과 시비가 붙어 무의식중에 망치를 휘둘렀다가 그를 죽이고 만다. 살인의 죄책감과 공포에 시달리던 이탕은 어느 날 자기가 죽인 이가 “죽어 마땅한 짓”을 저질러온 연쇄살인범이라는 사실을 알게 되고...',
     'https://image.aladin.co.kr/product/33336/18/cover500/k742938699_1.jpg',
     @now, @now),

    ('9791192128610', '녹색 자본론', '나카자와 신이치, 구혜원', '북드라망',
     '일본의 저명한 인류학자 나카자와 신이치가 21세기 들어 더욱 파괴적인 양상을 띠는 문명 세계와 자본주의의 한계를 마주하여 인류학적 관점에서 대안을 모색한 책이다. 이 책에 실린 총 4편의 글들은 모두 각기 다른 방식으로 오늘날의 자본주의가 무엇을 억압하고 배제해 왔는지를 말한다.',
     'https://image.aladin.co.kr/product/36890/80/cover500/k792030717_1.jpg',
     @now, @now),

    ('9791192625133', '거인의 어깨 1 - 벤저민 그레이엄, 워런 버핏, 피터 린치에게 배우다', '홍진채', '포레스트북스',
     '홍진채 저자의 해설로 살펴보는 시대를 관통하는 주식 대가들의 투자 철학과 투자법 탐구. 《거인의 어깨》는 총 3권으로 기획되었으며, 1권과 2권을 동시에 출간하고 3권은 2023년에 선보일 예정이다. 1권에서는 벤저민 그레이엄, 워런 버핏, 피터 린치의 투자법을 원점에서 살피고 대가들의 투자법을 실전에서 구현하기 위한 방법을 살펴본다.',
     'https://image.aladin.co.kr/product/30595/27/cover500/k092830014_1.jpg',
     @now, @now),

    ('9791193324530', '마지막 기도', '야쿠마루 가쿠, 남소현', '북플라자',
     '목사이자 교정위원인 호사카 소스케는 임신 중이던 딸 유아를 포함한 네 명의 여성을 무참히 살해한 범죄자 이시하라 료헤이와 마주하게 된다. 반성 없는 태도로 법정에서 “고마워요”라며 웃던 이시하라를 보며, 호사카는 복수심에 불타오른다. 하지만 교정위원으로서의 사명과 신앙, 그리고 인간으로서의 감정 사이에서 그는 깊은 갈등에 빠진다.',
     'https://image.aladin.co.kr/product/36571/50/cover500/k332039340_1.jpg',
     @now, @now),

    ('9791193394564', '어떻게 미국을 다시 위대하게 만들 것인가 (리커버판) - 트럼프의 정책과 비전이 담긴 유일한 저서', '도널드 트럼프, 김태훈', '이레미디어',
     '2016년에 출간한 《불구가 된 미국》의 리커버판으로, 역시 최근에 리커버판을 내놓은 원 출판사의 《Great Again》(1판 제목은 CRIPPLED AMERICA) 표지를 사용했다. 원 출판사가 제목을 바꾼 이유에 대해 추측을 하자면, 이 책이 가지는 특별함 때문일 것이다. 트럼프가 지금까지 출간한 그 어떤 책에도 없는, 정책과 비전이 담긴 유일한 책이기 때문이다.',
     'https://image.aladin.co.kr/product/35292/7/cover500/k252035768_1.jpg',
     @now, @now),

    ('9791193528723', '나이 들수록 행복해지는 인생의 태도에 관하여 - 103세 할머니 의사의 인생 수업', '글래디스 맥게리, 이주만', '부키',
     '\'전인의학의 어머니\'라 불리는 세계적 명의 글래디스 맥게리 박사가 자신의 80년 의료 활동과 100년이 넘는 인생 경험을 바탕으로 평생 즐겁고 활기찬 삶을 살아갈 수 있는 비밀을 들려준다. "어떻게 하면 죽는 날까지 건강하고 행복하게 살 수 있을까?" 우리 모두가 가장 간절히 바라는 이 소망에 대해 글래디스 박사는 우선 인생의 태도부터 바꾸라고 권한다.',
     'https://image.aladin.co.kr/product/36573/8/cover500/k332039343_1.jpg',
     @now, @now),

    ('9791193737330', '81ㅎ 한반도 X세대의 비밀', '이온', '공감s',
     '우리가 사는 이 세계는 단지 물질로 이루어진 차원이 아니다. 눈에 보이지 않는 진동의 강이 흐르고 있으며, 그 강의 맥을 잡는 자만이 운명의 설계도를 수정할 수 있다. 《81 한반도 X세대의 비밀》은 그 진동의 본래 구조를 복원하는 우주의 코드북이자, 81ㅎ 우주전사에게 내려진 우주의 작전 명령서다.',
     'https://image.aladin.co.kr/product/36769/9/cover500/k122030495_1.jpg',
     @now, @now),

    ('9791193790403', '해리 포터와 마법사의 돌 1 (무선)', 'J.K. 롤링, 강동혁', '문학수첩',
     '1997년 영국에서 출간된 이래 《해리 포터》 시리즈는 지금까지 200개국 이상 80개의 언어로 번역되고 출간되어 5억 부 이상을 판매했다. 국내에서도 1999년 《해리 포터와 마법사의 돌》의 출간을 필두로 지금까지 약 1,500만 부가 판매되었으며, 현재에도 독자들에게 변함없는 사랑을 받고 있다.',
     'https://image.aladin.co.kr/product/35144/78/cover500/k852934815_1.jpg',
     @now, @now),

    ('9791193790496', '해리 포터와 불의 잔 4 (무선)', 'J.K. 롤링, 강동혁', '문학수첩',
     '이번에 출간하는 《해리 포터》 시리즈는 지난 2019년에 새로운 번역을 선보인 버전이다. J.K. 롤링이 작품 속에 이룩해놓은 문학적 성취가 완벽하게 구현되어 있다. 복선과 반전을 선사하는 문학적 장치들을 보다 정교하고 세련되게 다듬었으며, 인물들 사이의 관계나 그들의 숨겨진 비밀 그리고 성격이 도드라지는 말투의 미세한 뉘앙스까지 점검했다.',
     'https://image.aladin.co.kr/product/35144/93/cover500/k322934816_1.jpg',
     @now, @now);

-- =========================
-- 5) club
-- =========================
INSERT INTO club (id, name, description, profile_img_url, region, insta, kakao, open, created_at, updated_at)
VALUES
    (1, '서울 독서모임', '서울에서 매주 모여 함께 읽어요',     NULL, '서울',   'checkmo_seoul', 'openchat_seoul', b'1', @now, @now),
    (2, '에세이 살롱',   '에세이/인문학을 좋아하는 사람들',     NULL, '부산',   'checkmo_essay', 'openchat_essay', b'0', @now, @now);

-- =========================
-- 6) club_interest_categories
-- =========================
INSERT INTO club_interest_categories (club_id, category)
VALUES
    (1, 'HUMANITIES'),
    (1, 'ESSAY'),
    (1, 'HISTORY_CULTURE'),
    (1, 'COMPUTER_IT'),
    (2, 'SELF_DEVELOPMENT'),
    (2, 'SCIENCE'),
    (2, 'ESSAY'),
    (2, 'ART_POP_CULTURE');

-- =========================
-- 7) club_participants
-- =========================
INSERT INTO club_participants (club_id, participant_type)
VALUES
    (1, 'OFFLINE'),
    (1, 'WORKER'),
    (1, 'STUDENT'),
    (2, 'ONLINE'),
    (2, 'WORKER'),
    (2, 'STUDENT');

-- =========================
-- 8) club_member
-- =========================
INSERT INTO club_member (id, club_id, member_id, club_member_status, join_message, created_at, updated_at)
VALUES
    (1,1, 'mem-001', 'STAFF',  '운영진으로 참여합니다!', @now, @now),
    (2,1, 'mem-002', 'MEMBER', '인문학 좋아해요',        @now, @now),
    (3,1, 'mem-003', 'PENDING','가입하고 싶습니다',      @now, @now),
    (4,2, 'mem-001', 'MEMBER',  '에세이 같이 읽어요',     @now, @now),
    (5,2, 'mem-002', 'MEMBER', '에세이 관심',           @now, @now),
    (6,2, 'mem-003', 'STAFF', '에세이 읽고 싶어요',     @now, @now);

-- =========================
-- 9) meeting
-- =========================
INSERT INTO meeting (id, club_id, book_id, title, content, location, meeting_time, generation, sum_rate, tag, version, created_at, updated_at)
VALUES
    (1, 1, '9791192005317', '살인자ㅇ난감 함께 읽기', '1~3화 감상/토론',        '강남역 2번 출구', DATE_ADD(@now, INTERVAL 3 DAY), 1, 0.0, 'MEETING', 1, @now, @now),
    (2, 1, '9791192625133', '거인의 어깨 1 스터디',  '투자 철학 요약/토론',     '디스코드',        DATE_ADD(@now, INTERVAL 5 DAY), 1, 0.0, 'MEETING', 1, @now, @now),
    (3, 2, '9791193528723', '인생의 태도 모임',      '좋아하는 문장 공유',      '서면 카페',        DATE_ADD(@now, INTERVAL 7 DAY), 1, 0.0, 'MEETING', 1, @now, @now);

-- =========================
-- 10) team
-- =========================
INSERT INTO team (id, meeting_id, team_number, created_at, updated_at)
VALUES
    (1,1, 1, @now, @now),
    (2,1, 2, @now, @now),
    (3,2, 1, @now, @now),
    (4,2, 2, @now, @now);

-- =========================
-- 11) topic
-- =========================
INSERT INTO topic (id,meeting_id, club_member_id, member_id, description, created_at, updated_at)
VALUES
    (1,1, 1, 'mem-001', '살인자ㅇ난감: 사건의 우연성과 도덕적 딜레마 포인트 정리', @now, @now),
    (2,1, 2, 'mem-002', '살인자ㅇ난감: 주인공 심리 변화와 서스펜스 장치 분석',     @now, @now),
    (3,1, 2, 'mem-002', '살인자ㅇ난감: 인상 깊은 장면/대사 공유',                 @now, @now),

    (4,2, 1, 'mem-001', '거인의 어깨: 그레이엄/버핏/린치 핵심 철학 비교',          @now, @now),
    (5,2, 1, 'mem-001', '거인의 어깨: 실전 적용 시 체크리스트/리스크 관리',       @now, @now),
    (6,2, 2, 'mem-002', '거인의 어깨: 내가 가져갈 투자 원칙 3가지',               @now, @now),

    (7,3, 1, 'mem-001', '인생의 태도: 마음가짐이 행동으로 이어지는 사례 공유',     @now, @now),
    (8,3, 2, 'mem-002', '인생의 태도: 가장 와닿은 문장/챕터 요약',                @now, @now),
    (9,3, 3, 'mem-003', '인생의 태도: 일상에 적용할 작은 습관 1개 제안',          @now, @now);

-- =========================
-- 12) team_topic
-- team은 (meeting_id, team_number)만 있으니 team.id를 조회해서 매핑
-- topic.id는 AUTO_INCREMENT라서, meeting_id 기반으로 골라서 매핑
-- =========================
INSERT INTO team_topic (team_id, topic_id, version, created_at, updated_at)
VALUES
    (1,1, 1, @now, @now),
    (1,2, 1, @now, @now),
    (2,3, 1, @now, @now),
    (3,4, 1, @now, @now),
    (3,5, 1, @now, @now),
    (4,6, 1, @now, @now),
    (4,7, 1, @now, @now),
    (4,9, 1, @now, @now);

-- =========================
-- 13) book_story
-- =========================
INSERT INTO book_story (id, member_id, book_id, title, description, likes, comments_count, view_count, deleted, deleted_at, created_at, updated_at)
VALUES
    (1, 'mem-001', '9791193324530', '마지막 기도 감상', '복수와 신앙 사이의 갈등이 인상적', 2, 1, 15, b'0', NULL, @now, @now),
    (2, 'mem-001', '9791192128610', '녹색 자본론 정리', '억압/배제의 구조를 인류학적으로 읽다', 1, 0, 8, b'0', NULL, @now, @now),
    (3, 'mem-002', '9791192005317', '살인자ㅇ난감 메모', '장면 전환 템포가 좋았던 포인트', 0, 0, 3, b'0', NULL, @now, @now),
    (4, 'mem-002', '9791192625133', '거인의 어깨 요약', '투자 대가들의 공통점/차이점 정리', 0, 2, 12, b'0', NULL, @now, @now),
    (5, 'mem-003', '9791193528723', '인생의 태도 한 문장', '태도가 결국 선택을 바꾼다', 1, 0, 5, b'0', NULL, @now, @now),
    (6, 'mem-003', '9791193790403', '해리포터 1권 재독', '초반 세계관 소개가 완벽한 입문서', 0, 1, 20, b'0', NULL, @now, @now),
    (7, 'mem-003', '9791193790496', '불의 잔 4권 포인트', '전개의 밀도와 복선 회수 메모', 2, 0, 10, b'0', NULL, @now, @now),
    (8, 'mem-003', '9791192005317', '드라마 먼저 보고 본 살인자ㅇ난감 후기', '영화보다 확실히 내용도 더 자세하게 뭔가 깊이가 있어서 많은 생각을 할 수 있었어요', 0, 0, 0, b'0', NULL, @now, @now),
    (9, 'mem-002', '9791193394564', '트럼프 정책 정리', '미국 우선주의 정책의 핵심 요약', 0, 0, 2, b'1', @now, @now, @now);

-- =========================
-- 14) book_story_liked
-- =========================
INSERT INTO book_story_liked (id,book_story_id, member_id, created_at, updated_at)
VALUES
    (101,1, 'mem-002', @now, @now),
    (102,1, 'mem-003', @now, @now),

    (103,2, 'mem-002', @now, @now),

    (104,5, 'mem-001', @now, @now),

    (105,7, 'mem-001', @now, @now),
    (106,7, 'mem-002', @now, @now);

-- =========================
-- 15) comment
-- =========================
INSERT INTO comment (id, book_story_id, parent_comment_id, member_id, content, deleted, deleted_at, created_at, updated_at)
VALUES
    (1, 1, NULL, 'mem-001', '저도 이 지점에서 주인공이 급격히 흔들리는 게 인상적이었어요.', b'1', @now, @now, @now),
    (2, 1, NULL, 'mem-002', '주인공의 위기 상황 때 정말 심장이 철렁했어요...', b'0', NULL, @now, @now),

    (3, 4, NULL, 'mem-003', '세 사람(그레이엄/버핏/린치) 관점 정리 너무 좋네요. 저는 리스크 관리 파트가 기억에 남았어요.', b'0', NULL, @now, @now),
    (4, 4, 2,    'mem-002', '맞아요. 특히 "잃지 않는 것"을 우선으로 보는 관점이 현실적이더라고요.', b'0', NULL, @now, @now),

    (5, 6, NULL, 'mem-001', '입문서로서 완성도가 진짜 높죠. 세계관 소개가 과하지 않으면서도 몰입되더라구요.', b'0', NULL, @now, @now);

-- =========================
-- 16) follow
-- =========================
INSERT INTO follow (id,follower_id, following_id, created_at, updated_at)
VALUES
    (201,'mem-001', 'mem-003', @now, @now),
    (202,'mem-002', 'mem-003', @now, @now),
    (203,'mem-003', 'mem-001', @now, @now),
    (204,'mem-003', 'mem-002', @now, @now);

-- =========================
-- 17) vote
-- =========================
INSERT INTO vote (id, club_id, title, content, tag, important, anonymity, duplication, start_time, deadline,
                  item1, item2, item3, item4, item5, created_at, updated_at)
VALUES
    (1, 1, '다음 모임 시간 투표', '선호 시간을 골라주세요', 'VOTE', b'1', b'0', b'0',
     @now, DATE_ADD(@now, INTERVAL 14 DAY),
     '토요일 오전', '토요일 오후', '일요일 오전', NULL, NULL, @now, @now);

-- =========================
-- 18) club_member_vote
-- =========================
INSERT INTO club_member_vote (vote_id, club_member_id, item1, item2, item3, item4, item5)
VALUES
    (1, 1, b'1', b'0', b'0', b'0', b'0'),
    (1, 2, b'0', b'1', b'0', b'0', b'0');

-- =========================
-- 19) notice
-- =========================
INSERT INTO notice (id, club_id, meeting_id, meeting_version, title, content, tag, important, created_at, updated_at)
VALUES
    (1, 1, 1, 1, '[모임공지] 살인자ㅇ난감 함께 읽기', '모임 정보가 생성되었습니다. 장소/시간 확인 후 참여 부탁드려요.', 'MEETING', b'1', @now, @now),
    (2, 1, 2, 1, '[모임공지] 거인의 어깨 1 스터디',  '모임 정보가 생성되었습니다. 토론 주제는 투자 철학 요약/비교입니다.', 'MEETING', b'1', @now, @now),
    (3, 2, 3, 1, '[모임공지] 인생의 태도 모임',      '좋아하는 문장을 한 문장씩 공유하는 방식으로 진행합니다.',         'MEETING', b'1', @now, @now),
    (4, 1, NULL, 1, '[공지] 모임 규칙', '장소/시간 확인 부탁드립니다.', 'NOTICE', b'1', @now, @now),
    (5, 2, NULL, 1, '발제 자료 공유',         '발제 예제 링크를 공유합니다.', 'NOTICE', b'0', @now, @now);

-- =========================
-- 20) notification
-- 알림 타입: LIKE / COMMENT / FOLLOW / JOIN_CLUB / CLUB_MEETING_CREATED / CLUB_NOTICE_CREATED
-- source_id: 알림 출처 엔티티 ID, domain_id: 대상 도메인 ID (FOLLOW는 null)
-- =========================
INSERT INTO notification (
    id, receiver_id, sender_id, source_id, domain_id, notification_type,
    is_read, created_at, updated_at
)
VALUES
    -- ===== LIKE 알림 (source_id = book_story_liked.id, domain_id = book_story_id) =====
    (1, 'mem-001', 'mem-002', 101, 1, 'LIKE', b'0', @now, @now),
    (2, 'mem-001', 'mem-003', 102, 1, 'LIKE', b'0', @now, @now),
    (3, 'mem-001', 'mem-002', 103, 2, 'LIKE', b'1', @now, @now),
    (4, 'mem-003', 'mem-001', 104, 5, 'LIKE', b'0', @now, @now),
    (5, 'mem-003', 'mem-001', 105, 7, 'LIKE', b'0', @now, @now),
    (6, 'mem-003', 'mem-002', 106, 7, 'LIKE', b'1', @now, @now),

    -- ===== COMMENT 알림 (source_id = comment.id, domain_id = book_story_id) =====
    (7, 'mem-001', 'mem-002', 1, 1, 'COMMENT', b'0', @now, @now),
    (8, 'mem-002', 'mem-003', 2, 4, 'COMMENT', b'0', @now, @now),
    (9, 'mem-003', 'mem-001', 4, 6, 'COMMENT', b'1', @now, @now),

    -- ===== FOLLOW 알림 (source_id = follow.id, domain_id = null) =====
    (10, 'mem-003', 'mem-001', 201, NULL, 'FOLLOW', b'0', @now, @now),
    (11, 'mem-003', 'mem-002', 202, NULL, 'FOLLOW', b'0', @now, @now),
    (12, 'mem-001', 'mem-003', 203, NULL, 'FOLLOW', b'1', @now, @now),
    (13, 'mem-002', 'mem-003', 204, NULL, 'FOLLOW', b'0', @now, @now),

    -- ===== JOIN_CLUB 알림 (source_id = club_member.id, domain_id = club_id) =====
    (14, 'mem-002', 'SYSTEM', 2, 1, 'JOIN_CLUB', b'0', @now, @now),
    (15, 'mem-001', 'SYSTEM', 4, 2, 'JOIN_CLUB', b'0', @now, @now),
    (16, 'mem-002', 'SYSTEM', 5, 2, 'JOIN_CLUB', b'1', @now, @now),

    -- ===== CLUB_MEETING_CREATED 알림 (source_id = meeting.id, domain_id = club_id) =====
    (17, 'mem-001', 'SYSTEM', 1, 1, 'CLUB_MEETING_CREATED', b'0', @now, @now),
    (18, 'mem-002', 'SYSTEM', 1, 1, 'CLUB_MEETING_CREATED', b'0', @now, @now),
    (19, 'mem-001', 'SYSTEM', 2, 1, 'CLUB_MEETING_CREATED', b'1', @now, @now),
    (20, 'mem-002', 'SYSTEM', 2, 1, 'CLUB_MEETING_CREATED', b'0', @now, @now),
    (21, 'mem-001', 'SYSTEM', 3, 2, 'CLUB_MEETING_CREATED', b'0', @now, @now),
    (22, 'mem-002', 'SYSTEM', 3, 2, 'CLUB_MEETING_CREATED', b'0', @now, @now),

    -- ===== CLUB_NOTICE_CREATED 알림 (source_id = notice.id, domain_id = club_id) =====
    (23, 'mem-001', 'SYSTEM', 4, 1, 'CLUB_NOTICE_CREATED', b'0', @now, @now),
    (24, 'mem-002', 'SYSTEM', 4, 1, 'CLUB_NOTICE_CREATED', b'0', @now, @now),
    (25, 'mem-001', 'SYSTEM', 5, 2, 'CLUB_NOTICE_CREATED', b'1', @now, @now),
    (26, 'mem-002', 'SYSTEM', 5, 2, 'CLUB_NOTICE_CREATED', b'0', @now, @now);
