-- =========================
-- 책 좋아요 목록 조회 테스트용 시드 데이터
-- =========================
SET @now := NOW(6);

-- mem-001 좋아요 책
INSERT IGNORE INTO book_liked (book_id, member_id, created_at, updated_at)
VALUES
    ('9791192005317', 'mem-001', @now, @now),
    ('9791192128610', 'mem-001', @now, @now),
    ('9791193790403', 'mem-001', @now, @now),
    ('9791193528723', 'mem-001', @now, @now);

-- mem-002 좋아요 책
INSERT IGNORE INTO book_liked (book_id, member_id, created_at, updated_at)
VALUES
    ('9791192005317', 'mem-002', @now, @now),
    ('9791192625133', 'mem-002', @now, @now),
    ('9791193790496', 'mem-002', @now, @now),
    ('9791193528723', 'mem-002', @now, @now);

-- mem-003 좋아요 책
INSERT IGNORE INTO book_liked (book_id, member_id, created_at, updated_at)
VALUES
    ('9791193324530', 'mem-003', @now, @now),
    ('9791193790403', 'mem-003', @now, @now),
    ('9791193394564', 'mem-003', @now, @now),
    ('9791192128610', 'mem-003', @now, @now);

-- book.likes 동기화
UPDATE book b
SET b.likes = (
    SELECT COUNT(*)
    FROM book_liked bl
    WHERE bl.book_id = b.id
);
