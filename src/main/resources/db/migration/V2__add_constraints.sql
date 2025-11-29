-- 유니크 제약

-- AuthUser (이메일 중복 방지)
ALTER TABLE auth_user
    ADD CONSTRAINT UK_auth_user_email UNIQUE (email);

-- Club (클럽 이름 중복 방지)
ALTER TABLE club
    ADD CONSTRAINT UK_club_name UNIQUE (name);

-- BookStoryLiked (한 멤버가 한 스토리에 중복 좋아요 방지)
ALTER TABLE book_story_liked
    ADD CONSTRAINT UK_book_story_liked_member_book_story UNIQUE (member_id, book_story_id);

-- ClubMemberVote (한 멤버가 한 투표에 중복 참여 방지)
ALTER TABLE club_member_vote
    ADD CONSTRAINT UK_club_member_vote_member_vote UNIQUE (club_member_id, vote_id);

-- Follow (중복 팔로우 방지)
ALTER TABLE follow
    ADD CONSTRAINT UK_follow_follower_following UNIQUE (follower_id, following_id);

-- Notification (동일한 이벤트에 대한 중복 알림 방지)
ALTER TABLE notification
    ADD CONSTRAINT UK_notification_type_source UNIQUE (notification_type, source_id);

-- Team (한 미팅 내에서 팀 번호 중복 방지)
ALTER TABLE team
    ADD CONSTRAINT UK_team_meeting_team_number UNIQUE (meeting_id, team_number);

-- TeamTopic (한 팀이 한 토픽을 중복 선택 방지)
ALTER TABLE team_topic
    ADD CONSTRAINT UK_team_topic_topic_team UNIQUE (topic_id, team_id);

-- 외래키 제약

-- BookRecommend -> ClubMember
ALTER TABLE book_recommend
    ADD CONSTRAINT FK_book_recommend_club_member
        FOREIGN KEY (club_member_id) REFERENCES club_member (id);

-- BookReview -> Meeting
ALTER TABLE book_review
    ADD CONSTRAINT FK_book_review_meeting
        FOREIGN KEY (meeting_id) REFERENCES meeting (id);

-- BookStoryLiked -> BookStory
ALTER TABLE book_story_liked
    ADD CONSTRAINT FK_book_story_liked_book_story
        FOREIGN KEY (book_story_id) REFERENCES book_story (id);

-- ClubInterestCategories -> Club
ALTER TABLE club_interest_categories
    ADD CONSTRAINT FK_club_interest_categories_club
        FOREIGN KEY (club_id) REFERENCES club (id);

-- ClubParticipants -> Club
ALTER TABLE club_participants
    ADD CONSTRAINT FK_club_participants_club
        FOREIGN KEY (club_id) REFERENCES club (id);

-- ClubMember -> Club
ALTER TABLE club_member
    ADD CONSTRAINT FK_club_member_club
        FOREIGN KEY (club_id) REFERENCES club (id);

-- ClubMemberTeam -> Team
ALTER TABLE club_member_team
    ADD CONSTRAINT FK_club_member_team_team
        FOREIGN KEY (team_id) REFERENCES team (id);

-- ClubMemberVote -> Vote
ALTER TABLE club_member_vote
    ADD CONSTRAINT FK_club_member_vote_vote
        FOREIGN KEY (vote_id) REFERENCES vote (id);

-- Comment -> BookStory
ALTER TABLE comment
    ADD CONSTRAINT FK_comment_book_story
        FOREIGN KEY (book_story_id) REFERENCES book_story (id);

-- Comment -> Comment (대댓글: 부모 댓글 참조)
ALTER TABLE comment
    ADD CONSTRAINT FK_comment_parent
        FOREIGN KEY (parent_comment_id) REFERENCES comment (id);

-- Follow -> Member (Follower)
ALTER TABLE follow
    ADD CONSTRAINT FK_follow_follower
        FOREIGN KEY (follower_id) REFERENCES member (id);

-- Follow -> Member (Following)
ALTER TABLE follow
    ADD CONSTRAINT FK_follow_following
        FOREIGN KEY (following_id) REFERENCES member (id);

-- MemberInterestCategories -> Member
ALTER TABLE member_interest_categories
    ADD CONSTRAINT FK_member_interest_categories_member
        FOREIGN KEY (member_id) REFERENCES member (id);

-- Team -> Meeting
ALTER TABLE team
    ADD CONSTRAINT FK_team_meeting
        FOREIGN KEY (meeting_id) REFERENCES meeting (id);

-- TeamTopic -> Team
ALTER TABLE team_topic
    ADD CONSTRAINT FK_team_topic_team
        FOREIGN KEY (team_id) REFERENCES team (id);

-- TeamTopic -> Topic
ALTER TABLE team_topic
    ADD CONSTRAINT FK_team_topic_topic
        FOREIGN KEY (topic_id) REFERENCES topic (id);

-- Topic -> Meeting
ALTER TABLE topic
    ADD CONSTRAINT FK_topic_meeting
        FOREIGN KEY (meeting_id) REFERENCES meeting (id);