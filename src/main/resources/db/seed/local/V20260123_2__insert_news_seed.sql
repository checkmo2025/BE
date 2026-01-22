-- =========================
-- News 테스트 데이터
-- =========================
SET @now := NOW(6);

INSERT INTO news (id, title, requester_email, content, thumbnail_url, original_link, publish_start_at, publish_end_at, created_at, updated_at)
VALUES
    (1, '책모 앱 정식 출시 안내',
     'admin@checkmo.local',
     '안녕하세요, 책모입니다. 오랜 준비 끝에 드디어 책모 앱이 정식 출시되었습니다! 이제 더 편리하게 독서 모임을 관리하고, 책 이야기를 나눌 수 있습니다. 많은 관심과 사랑 부탁드립니다.',
     'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400',
     'https://checkmo.local/news/1',
     '2026-01-01', '2026-12-31',
     @now, @now),

    (2, '1월 독서 챌린지 이벤트',
     'admin@checkmo.local',
     '새해를 맞아 1월 독서 챌린지를 진행합니다! 이번 달 동안 책 5권을 읽고 책 이야기를 작성하시면 추첨을 통해 도서 상품권을 드립니다. 참여 방법: 1) 책 읽기 2) 책 이야기 작성 3) #1월챌린지 태그 달기',
     'https://images.unsplash.com/photo-1507842217343-583bb7270b66?w=400',
     'https://checkmo.local/events/january-challenge',
     '2026-01-01', '2026-01-31',
     @now, @now),

    (3, '신규 기능 업데이트: 모임 일정 알림',
     'admin@checkmo.local',
     '모임 일정을 놓치지 않도록 알림 기능이 추가되었습니다. 모임 하루 전, 1시간 전에 푸시 알림을 받을 수 있습니다. 설정에서 알림을 켜고 중요한 모임을 놓치지 마세요!',
     'https://images.unsplash.com/photo-1611532736597-de2d4265fba3?w=400',
     NULL,
     '2026-01-10', '2026-02-28',
     @now, @now),

    (4, '독서모임 운영 가이드 공개',
     'admin@checkmo.local',
     '성공적인 독서모임 운영을 위한 가이드를 공개합니다. 모임 주제 선정부터 토론 진행 방법, 멤버 관리 팁까지 상세하게 안내해 드립니다. 처음 독서모임을 시작하시는 분들께 도움이 되길 바랍니다.',
     'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400',
     'https://checkmo.local/guide/book-club',
     '2026-01-15', '2026-06-30',
     @now, @now),

    (5, '서버 점검 안내 (1/25)',
     'admin@checkmo.local',
     '서비스 개선을 위한 정기 점검이 예정되어 있습니다. 점검 시간: 2026년 1월 25일 02:00 ~ 06:00 (약 4시간). 점검 중에는 서비스 이용이 불가합니다. 이용에 불편을 드려 죄송합니다.',
     NULL,
     NULL,
     '2026-01-20', '2026-01-25',
     @now, @now),

    (6, '2월 북토크 행사 사전 신청',
     'admin@checkmo.local',
     '2월에 진행되는 특별 북토크 행사에 여러분을 초대합니다! 베스트셀러 작가와 함께하는 온라인 북토크로, 선착순 100명에게 참여 기회를 드립니다. 관심 있으신 분들은 사전 신청해 주세요.',
     'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=400',
     'https://checkmo.local/events/february-booktalk',
     '2026-01-22', '2026-02-10',
     @now, @now);

-- =========================
-- NewsImage 테스트 데이터
-- =========================
INSERT INTO news_image (id, news_id, image_url, sort_order, created_at, updated_at)
VALUES
    (1, 1, 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800', 0, @now, @now),
    (2, 1, 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=800', 1, @now, @now),
    (3, 2, 'https://images.unsplash.com/photo-1507842217343-583bb7270b66?w=800', 0, @now, @now),
    (4, 4, 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=800', 0, @now, @now),
    (5, 4, 'https://images.unsplash.com/photo-1519682337058-a94d519337bc?w=800', 1, @now, @now),
    (6, 6, 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800', 0, @now, @now);