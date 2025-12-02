-- 유니크 제약

ALTER TABLE auth_user
    ADD CONSTRAINT UK_auth_user_email UNIQUE (email);

ALTER TABLE member
    ADD CONSTRAINT UK_member_email UNIQUE (email);

ALTER TABLE member
    ADD CONSTRAINT UK_member_nickname UNIQUE (nick_name);

ALTER TABLE club
    ADD CONSTRAINT UK_club_name UNIQUE (name);

ALTER TABLE book_story_liked
    ADD CONSTRAINT UK_book_story_liked_member_book_story UNIQUE (member_id, book_story_id);

ALTER TABLE club_member_vote
    ADD CONSTRAINT UK_club_member_vote_member_vote UNIQUE (club_member_id, vote_id);

ALTER TABLE follow
    ADD CONSTRAINT UK_follow_follower_following UNIQUE (follower_id, following_id);

ALTER TABLE notification
    ADD CONSTRAINT UK_notification_type_source UNIQUE (notification_type, source_id);

ALTER TABLE team
    ADD CONSTRAINT UK_team_meeting_team_number UNIQUE (meeting_id, team_number);

ALTER TABLE team_topic
    ADD CONSTRAINT UK_team_topic_topic_team UNIQUE (topic_id, team_id);

-- 외래키 제약

ALTER TABLE book_recommend
    ADD CONSTRAINT FK_book_recommend_club_member
        FOREIGN KEY (club_member_id) REFERENCES club_member (id);

ALTER TABLE book_review
    ADD CONSTRAINT FK_book_review_meeting
        FOREIGN KEY (meeting_id) REFERENCES meeting (id);

ALTER TABLE book_story_liked
    ADD CONSTRAINT FK_book_story_liked_book_story
        FOREIGN KEY (book_story_id) REFERENCES book_story (id);

ALTER TABLE club_interest_categories
    ADD CONSTRAINT FK_club_interest_categories_club
        FOREIGN KEY (club_id) REFERENCES club (id);

ALTER TABLE club_participants
    ADD CONSTRAINT FK_club_participants_club
        FOREIGN KEY (club_id) REFERENCES club (id);

ALTER TABLE club_member
    ADD CONSTRAINT FK_club_member_club
        FOREIGN KEY (club_id) REFERENCES club (id);

ALTER TABLE club_member_team
    ADD CONSTRAINT FK_club_member_team_team
        FOREIGN KEY (team_id) REFERENCES team (id);

ALTER TABLE club_member_vote
    ADD CONSTRAINT FK_club_member_vote_vote
        FOREIGN KEY (vote_id) REFERENCES vote (id);

ALTER TABLE comment
    ADD CONSTRAINT FK_comment_book_story
        FOREIGN KEY (book_story_id) REFERENCES book_story (id);

ALTER TABLE comment
    ADD CONSTRAINT FK_comment_parent
        FOREIGN KEY (parent_comment_id) REFERENCES comment (id);

ALTER TABLE follow
    ADD CONSTRAINT FK_follow_follower
        FOREIGN KEY (follower_id) REFERENCES member (id);

ALTER TABLE follow
    ADD CONSTRAINT FK_follow_following
        FOREIGN KEY (following_id) REFERENCES member (id);

ALTER TABLE member_interest_categories
    ADD CONSTRAINT FK_member_interest_categories_member
        FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE team
    ADD CONSTRAINT FK_team_meeting
        FOREIGN KEY (meeting_id) REFERENCES meeting (id);

ALTER TABLE team_topic
    ADD CONSTRAINT FK_team_topic_team
        FOREIGN KEY (team_id) REFERENCES team (id);

ALTER TABLE team_topic
    ADD CONSTRAINT FK_team_topic_topic
        FOREIGN KEY (topic_id) REFERENCES topic (id);

ALTER TABLE topic
    ADD CONSTRAINT FK_topic_meeting
        FOREIGN KEY (meeting_id) REFERENCES meeting (id);