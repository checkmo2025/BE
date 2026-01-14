-- 복합 PK 추가
ALTER TABLE club_interest_categories
ADD PRIMARY KEY (club_id, category);

-- 국내 도서, 어린이 도서 카테고리 명 변경
UPDATE club_interest_categories
SET category = 'RELIGION_PHILOSOPHY'
WHERE category = 'DOMESTIC';

UPDATE club_interest_categories
SET category = 'CHILDREN_BOOKS'
WHERE category = 'CHILDREN';

-- 불필요한 club_category 제거
DROP TABLE IF EXISTS club_category;