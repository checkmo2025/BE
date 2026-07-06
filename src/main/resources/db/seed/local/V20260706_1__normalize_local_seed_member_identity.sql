SET @old_foreign_key_checks := @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

UPDATE auth_user
SET id = CONCAT('LOCAL_', id)
WHERE id REGEXP '^mem-[0-9]{3}$';

UPDATE member
SET id = CONCAT('LOCAL_', id)
WHERE id REGEXP '^mem-[0-9]{3}$';

UPDATE member_interest_categories
SET member_id = CONCAT('LOCAL_', member_id)
WHERE member_id REGEXP '^mem-[0-9]{3}$';

UPDATE book_review
SET member_id = CONCAT('LOCAL_', member_id)
WHERE member_id REGEXP '^mem-[0-9]{3}$';

UPDATE book_story
SET member_id = CONCAT('LOCAL_', member_id)
WHERE member_id REGEXP '^mem-[0-9]{3}$';

UPDATE book_story_liked
SET member_id = CONCAT('LOCAL_', member_id)
WHERE member_id REGEXP '^mem-[0-9]{3}$';

UPDATE club_member
SET member_id = CONCAT('LOCAL_', member_id)
WHERE member_id REGEXP '^mem-[0-9]{3}$';

UPDATE comment
SET member_id = CONCAT('LOCAL_', member_id)
WHERE member_id REGEXP '^mem-[0-9]{3}$';

UPDATE follow
SET follower_id = CONCAT('LOCAL_', follower_id)
WHERE follower_id REGEXP '^mem-[0-9]{3}$';

UPDATE follow
SET following_id = CONCAT('LOCAL_', following_id)
WHERE following_id REGEXP '^mem-[0-9]{3}$';

UPDATE notification
SET receiver_id = CONCAT('LOCAL_', receiver_id)
WHERE receiver_id REGEXP '^mem-[0-9]{3}$';

UPDATE notification
SET sender_id = CONCAT('LOCAL_', sender_id)
WHERE sender_id REGEXP '^mem-[0-9]{3}$';

UPDATE topic
SET member_id = CONCAT('LOCAL_', member_id)
WHERE member_id REGEXP '^mem-[0-9]{3}$';

UPDATE notification_setting
SET member_id = CONCAT('LOCAL_', member_id)
WHERE member_id REGEXP '^mem-[0-9]{3}$';

UPDATE member_terms
SET member_id = CONCAT('LOCAL_', member_id)
WHERE member_id REGEXP '^mem-[0-9]{3}$';

UPDATE book_liked
SET member_id = CONCAT('LOCAL_', member_id)
WHERE member_id REGEXP '^mem-[0-9]{3}$';

UPDATE member_block
SET blocker_id = CONCAT('LOCAL_', blocker_id)
WHERE blocker_id REGEXP '^mem-[0-9]{3}$';

UPDATE member_block
SET blocked_id = CONCAT('LOCAL_', blocked_id)
WHERE blocked_id REGEXP '^mem-[0-9]{3}$';

UPDATE report
SET reporter_id = CONCAT('LOCAL_', reporter_id)
WHERE reporter_id REGEXP '^mem-[0-9]{3}$';

UPDATE team_chat_message
SET sender_member_id = CONCAT('LOCAL_', sender_member_id)
WHERE sender_member_id REGEXP '^mem-[0-9]{3}$';

UPDATE push_device
SET member_id = CONCAT('LOCAL_', member_id)
WHERE member_id REGEXP '^mem-[0-9]{3}$';

SET FOREIGN_KEY_CHECKS = @old_foreign_key_checks;
