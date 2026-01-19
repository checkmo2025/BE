-- 기존 회원들에 대해 NotificationSetting 생성
INSERT INTO notification_setting (member_id, book_story_liked, book_story_comment, club_notice_created, club_meeting_created, new_follower, join_club, created_at, updated_at)
SELECT id, 1, 1, 1, 1, 1, 1, NOW(), NOW()
FROM member;