-- notice 테이블에 vote_id 컬럼 추가
ALTER TABLE notice
ADD COLUMN vote_id BIGINT NULL;

-- notice 테이블에 기존 NOTICE 태그를 GENERAL 태그로 변경
UPDATE notice
SET tag = 'GENERAL'
WHERE tag = 'NOTICE';

-- 기존 vote 테이블의 레코드를 notice로 생성
--  - tag는 'VOTE'
--  - meeting_id, meeting_version은 NULL
--  - club_id, title, content, important, created_at, updated_at은 vote 값 사용
--  - vote_id는 vote.id로 연결
INSERT INTO notice (
    club_id,
    meeting_id,
    meeting_version,
    title,
    content,
    tag,
    important,
    vote_id,
    created_at,
    updated_at
)
SELECT
    v.club_id,
    NULL,
    NULL,
    v.title,
    v.content,
    'VOTE',
    v.important,
    v.id,
    v.created_at,
    v.updated_at
FROM vote v;

-- vote_id unique 제약 조건 추가
ALTER TABLE notice
ADD CONSTRAINT UK_notice_vote_id UNIQUE (vote_id);

-- vote_id fk 조건 추가
ALTER TABLE notice
ADD CONSTRAINT FK_notice_vote
FOREIGN KEY (vote_id) REFERENCES vote (id);

-- vote 테이블에서 tag, important, club_id 컬럼 제거
ALTER TABLE vote
DROP COLUMN tag,
DROP COLUMN important,
DROP COLUMN club_id;