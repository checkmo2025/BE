-- =========================
-- Book story draft/published samples for local mypage testing
-- =========================
SET @now := NOW(6);

INSERT INTO book_story (
    member_id,
    book_id,
    title,
    description,
    status,
    likes,
    comments_count,
    view_count,
    deleted,
    deleted_at,
    created_at,
    updated_at
)
VALUES
    (
        'mem-003',
        '9791192128610',
        '녹색 자본론 다시 읽기 메모',
        NULL,
        'DRAFT',
        0,
        0,
        0,
        b'0',
        NULL,
        DATE_ADD(@now, INTERVAL 2 MINUTE),
        DATE_ADD(@now, INTERVAL 2 MINUTE)
    ),
    (
        'mem-003',
        '9791193324530',
        '마지막 기도 초안',
        '주인공의 복수심과 신앙 사이의 갈등을 조금 더 정리해볼 예정',
        'DRAFT',
        0,
        0,
        0,
        b'0',
        NULL,
        DATE_ADD(@now, INTERVAL 1 MINUTE),
        DATE_ADD(@now, INTERVAL 1 MINUTE)
    ),
    (
        'mem-003',
        '9791192625133',
        '거인의 어깨 투자 원칙 정리',
        '벤저민 그레이엄, 워런 버핏, 피터 린치의 관점을 비교하면서 읽은 내용을 정리했다.',
        'PUBLISHED',
        0,
        0,
        0,
        b'0',
        NULL,
        DATE_SUB(@now, INTERVAL 1 MINUTE),
        DATE_SUB(@now, INTERVAL 1 MINUTE)
    );
