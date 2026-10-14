-- MySQL 8, 읽기 전용. 전환 전/후 같은 DB에서 실행하여 결과를 보관한다.
-- ISBN/상품번호 치환, 소개/표지 갱신, 데이터 삭제를 수행하지 않는다.
START TRANSACTION WITH CONSISTENT SNAPSHOT, READ ONLY;

SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'book'
  AND COLUMN_NAME IN ('id', 'description');

SELECT 'book' AS table_name, COUNT(*) AS row_count FROM book
UNION ALL SELECT 'meeting', COUNT(*) FROM meeting
UNION ALL SELECT 'book_story', COUNT(*) FROM book_story
UNION ALL SELECT 'book_liked', COUNT(*) FROM book_liked;

-- 외래키가 없는 모듈 간 ID 참조도 검사한다. 이상값은 자동 수정하지 않는다.
SELECT 'meeting' AS table_name, COUNT(*) AS orphan_count
FROM meeting r LEFT JOIN book b ON b.id = r.book_id WHERE b.id IS NULL
UNION ALL
SELECT 'book_story', COUNT(*)
FROM book_story r LEFT JOIN book b ON b.id = r.book_id WHERE b.id IS NULL
UNION ALL
SELECT 'book_liked', COUNT(*)
FROM book_liked r LEFT JOIN book b ON b.id = r.book_id WHERE b.id IS NULL;

WITH RECURSIVE positions(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM positions WHERE n < 13
), isbn_status AS (
    SELECT b.id,
        CASE WHEN b.id REGEXP '^97[89][0-9]{10}$' THEN
            MOD(SUM(CAST(SUBSTRING(b.id, p.n, 1) AS UNSIGNED)
                    * CASE WHEN MOD(p.n, 2) = 0 THEN 3 ELSE 1 END), 10) = 0
        ELSE FALSE END AS valid_isbn13
    FROM book b CROSS JOIN positions p GROUP BY b.id
)
SELECT COUNT(*) AS total_books,
       COALESCE(SUM(valid_isbn13), 0) AS valid_isbn13_count,
       COALESCE(SUM(NOT valid_isbn13), 0) AS review_without_rewriting_count
FROM isbn_status;

SELECT COUNT(*) AS total_books,
       COALESCE(SUM(CHAR_LENGTH(description) > 255), 0) AS long_descriptions,
       COALESCE(SUM(img_url LIKE '%image.aladin.co.kr/%'), 0) AS aladin_cover_urls,
       COALESCE(SUM(img_url LIKE '%image.yes24.com/%'), 0) AS yes24_cover_urls
FROM book;

SELECT COUNT(*) AS books_with_likes_count_mismatch
FROM book b LEFT JOIN (
    SELECT book_id, COUNT(*) AS actual_likes FROM book_liked GROUP BY book_id
) l ON l.book_id = b.id
WHERE b.likes <> COALESCE(l.actual_likes, 0);

COMMIT;
