UPDATE report
SET redirect_url = REPLACE(redirect_url, '/notices/', '/notice/')
WHERE report_target_type IN ('CLUB_NOTICE', 'CLUB_NOTICE_COMMENT')
  AND redirect_url LIKE '/groups/%/notices/%';

UPDATE report r
    JOIN topic t ON t.id = CAST(r.target_id AS UNSIGNED)
    JOIN meeting m ON m.id = t.meeting_id
SET r.redirect_url = CONCAT(
        '/groups/', m.club_id,
        '/bookcase/', m.id,
        '?tab=topic&topicId=', t.id
    )
WHERE r.report_target_type = 'CLUB_TOPIC';

UPDATE report r
    JOIN book_review br ON br.id = CAST(r.target_id AS UNSIGNED)
    JOIN meeting m ON m.id = br.meeting_id
SET r.redirect_url = CONCAT(
        '/groups/', m.club_id,
        '/bookcase/', m.id,
        '?tab=review&reviewId=', br.id
    )
WHERE r.report_target_type = 'CLUB_BOOK_REVIEW';

UPDATE report r
    JOIN team_chat_message tcm ON tcm.id = CAST(r.target_id AS UNSIGNED)
SET r.redirect_url = CONCAT(
        '/groups/', tcm.club_id,
        '/bookcase/', tcm.meeting_id,
        '/meeting?teamId=', tcm.team_id,
        '&messageId=', tcm.id
    )
WHERE r.report_target_type = 'CHAT';
