UPDATE auth_user a
    JOIN member m ON a.id = m.id -- 만약 ID가 외래키 관계라면 이대로 실행
    SET a.nick_name = m.nick_name
WHERE a.nick_name IS NULL;