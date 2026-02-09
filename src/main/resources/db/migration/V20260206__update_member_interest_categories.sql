-- 복합 PK 추가
ALTER TABLE member_interest_categories
    ADD PRIMARY KEY (member_id, category);

-- 국내 도서, 어린이 도서 카테고리 명 변경
UPDATE member_interest_categories
SET category = 'RELIGION_PHILOSOPHY'
WHERE category = 'DOMESTIC';

UPDATE member_interest_categories
SET category = 'CHILDREN_BOOKS'
WHERE category = 'CHILDREN';
