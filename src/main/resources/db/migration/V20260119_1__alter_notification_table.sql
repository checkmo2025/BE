-- 기존 유니크 제약조건 변경
ALTER TABLE notification
    DROP INDEX UK_notification_type_source;

ALTER TABLE notification
    ADD CONSTRAINT UK_notification_type_source_receiver UNIQUE (notification_type, source_id, receiver_id);

-- redirect_path, target_name 컬럼 삭제 및 domain_id 컬럼 추가
ALTER TABLE notification DROP COLUMN redirect_path;
ALTER TABLE notification DROP COLUMN target_name;

ALTER TABLE notification ADD COLUMN domain_id BIGINT NULL;