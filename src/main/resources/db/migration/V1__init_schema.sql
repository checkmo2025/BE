-- AuthUser
CREATE TABLE IF NOT EXISTS auth_user (
    id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    profile_completed BIT NOT NULL,
    deactivated_at DATETIME(6),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE (email)
) ENGINE=InnoDB;

-- Member
CREATE TABLE IF NOT EXISTS member (
    id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    nick_name VARCHAR(255) NOT NULL,
    description VARCHAR(20) NOT NULL,
    img_url VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE (email),
    UNIQUE (nick_name)
) ENGINE=InnoDB;

-- Member Interest Categories
CREATE TABLE IF NOT EXISTS member_interest_categories (
    member_id VARCHAR(255) NOT NULL,
    category VARCHAR(50)
) ENGINE=InnoDB;

-- Book
CREATE TABLE IF NOT EXISTS book (
    id VARCHAR(255) NOT NULL,
    title VARCHAR(255),
    author VARCHAR(255),
    publisher VARCHAR(255),
    description VARCHAR(255),
    img_url VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Book Recommend
CREATE TABLE IF NOT EXISTS book_recommend (
    id BIGINT NOT NULL AUTO_INCREMENT,
    book_id VARCHAR(255) NOT NULL,
    club_member_id BIGINT,
    title VARCHAR(255),
    content TEXT,
    tag VARCHAR(255),
    rate FLOAT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Book Review
CREATE TABLE IF NOT EXISTS book_review (
    id BIGINT NOT NULL AUTO_INCREMENT,
    meeting_id BIGINT,
    club_member_id BIGINT NOT NULL,
    member_id VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    rate FLOAT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Book Story
CREATE TABLE IF NOT EXISTS book_story (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id VARCHAR(255),
    book_id VARCHAR(255) NOT NULL,
    title VARCHAR(255),
    description TEXT,
    likes INTEGER NOT NULL,
    comments_count INTEGER NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Book Story Liked
CREATE TABLE IF NOT EXISTS book_story_liked (
    id BIGINT NOT NULL AUTO_INCREMENT,
    book_story_id BIGINT,
    member_id VARCHAR(255) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE (book_story_id, member_id)
) ENGINE=InnoDB;

-- Club
CREATE TABLE IF NOT EXISTS club (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    profile_img_url VARCHAR(255),
    region VARCHAR(255),
    insta VARCHAR(255),
    kakao VARCHAR(255),
    open BIT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE (name)
) ENGINE=InnoDB;

-- Club Interest Categories
CREATE TABLE IF NOT EXISTS club_interest_categories (
    club_id BIGINT NOT NULL,
    category VARCHAR(50)
) ENGINE=InnoDB;

-- Club Participants
CREATE TABLE IF NOT EXISTS club_participants (
    club_id BIGINT NOT NULL,
    participant_type VARCHAR(50)
) ENGINE=InnoDB;

-- Club Member
CREATE TABLE IF NOT EXISTS club_member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    club_id BIGINT,
    member_id VARCHAR(255) NOT NULL,
    club_member_status VARCHAR(20) NOT NULL,
    join_message VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Club Member Team
CREATE TABLE IF NOT EXISTS club_member_team (
    id BIGINT NOT NULL AUTO_INCREMENT,
    team_id BIGINT,
    club_member_id BIGINT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Club Member Vote
CREATE TABLE IF NOT EXISTS club_member_vote (
    id BIGINT NOT NULL AUTO_INCREMENT,
    vote_id BIGINT,
    club_member_id BIGINT NOT NULL,
    item1 BIT NOT NULL,
    item2 BIT NOT NULL,
    item3 BIT NOT NULL,
    item4 BIT NOT NULL,
    item5 BIT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (vote_id, club_member_id)
) ENGINE=InnoDB;

-- Comment
CREATE TABLE IF NOT EXISTS comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    book_story_id BIGINT,
    parent_comment_id BIGINT,
    member_id VARCHAR(255),
    content VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Follow
CREATE TABLE IF NOT EXISTS follow (
    id BIGINT NOT NULL AUTO_INCREMENT,
    follower_id VARCHAR(255),
    following_id VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE (follower_id, following_id)
) ENGINE=InnoDB;

-- Meeting
CREATE TABLE IF NOT EXISTS meeting (
    id BIGINT NOT NULL AUTO_INCREMENT,
    club_id BIGINT NOT NULL,
    book_id VARCHAR(255) NOT NULL,
    title VARCHAR(255),
    content VARCHAR(1000),
    location VARCHAR(255),
    meeting_time DATETIME(6),
    generation INTEGER NOT NULL,
    sum_rate FLOAT NOT NULL,
    tag VARCHAR(255),
    version BIGINT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Notice
CREATE TABLE IF NOT EXISTS notice (
    id BIGINT NOT NULL AUTO_INCREMENT,
    club_id BIGINT NOT NULL,
    meeting_id BIGINT,
    meeting_version BIGINT,
    title VARCHAR(255),
    content VARCHAR(1000),
    tag VARCHAR(50) NOT NULL,
    important BIT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Notification
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT NOT NULL AUTO_INCREMENT,
    receiver_id VARCHAR(255) NOT NULL,
    sender_id VARCHAR(255) NOT NULL,
    source_id BIGINT NOT NULL,
    target_name VARCHAR(255),
    notification_type VARCHAR(50) NOT NULL,
    redirect_path VARCHAR(255) NOT NULL,
    is_read BIT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE (notification_type, source_id)
) ENGINE=InnoDB;

-- Team
CREATE TABLE IF NOT EXISTS team (
    id BIGINT NOT NULL AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL,
    team_number INTEGER NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE (meeting_id, team_number)
) ENGINE=InnoDB;

-- Team Topic
CREATE TABLE IF NOT EXISTS team_topic (
    id BIGINT NOT NULL AUTO_INCREMENT,
    team_id BIGINT,
    topic_id BIGINT,
    version BIGINT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE (team_id, topic_id)
) ENGINE=InnoDB;

-- Topic
CREATE TABLE IF NOT EXISTS topic (
    id BIGINT NOT NULL AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL,
    club_member_id BIGINT NOT NULL,
    member_id VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Vote
CREATE TABLE IF NOT EXISTS vote (
    id BIGINT NOT NULL AUTO_INCREMENT,
    club_id BIGINT NOT NULL,
    title VARCHAR(255),
    content VARCHAR(255),
    tag VARCHAR(50),
    important BIT NOT NULL,
    anonymity BIT NOT NULL,
    duplication BIT NOT NULL,
    start_time DATETIME(6),
    deadline DATETIME(6),
    item1 VARCHAR(255) NOT NULL,
    item2 VARCHAR(255) NOT NULL,
    item3 VARCHAR(255),
    item4 VARCHAR(255),
    item5 VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Spring Modulith 이벤트 테이블
CREATE TABLE IF NOT EXISTS event_publication (
    id BINARY(16) NOT NULL,
    listener_id VARCHAR(255),
    event_type VARCHAR(255),
    serialized_event VARCHAR(255),
    publication_date DATETIME(6),
    completion_date DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;