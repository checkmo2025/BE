-- MEETING / VOTE_MEETING 태그 Notice에 한해 Meeting의 content 내용을 Notice content로 복사
UPDATE notice n
    JOIN meeting m ON m.id = n.meeting_id
    SET n.content = m.content
WHERE n.tag IN ('MEETING', 'VOTE_MEETING');

-- 미팅의 content 필드 삭제
ALTER TABLE meeting
DROP COLUMN content;

-- 미팅의 기수 필드를 nullable로 변경
ALTER TABLE meeting
MODIFY COLUMN generation INT NULL;