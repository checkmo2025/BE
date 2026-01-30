-- last_activity_at 컬럼 추가
ALTER TABLE club
    ADD COLUMN last_activity_at DATETIME NULL;

-- meeting의 updated_at 최대값으로 club.last_activity_at 초기화
UPDATE club c
JOIN (
    SELECT club_id, MAX(updated_at) AS last_meeting_at
    FROM meeting
    GROUP BY club_id
) m ON m.club_id = c.id
SET c.last_activity_at = m.last_meeting_at;

-- meeting이 없는 클럽은 created_at으로 초기화
UPDATE club
SET last_activity_at = created_at
WHERE last_activity_at IS NULL;