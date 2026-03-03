-- =========================
-- 회원 신고 목록 조회 테스트용 시드 데이터
-- =========================
SET @now := NOW(6);

-- mem-001이 신고한 데이터 (20개 초과로 커서 페이징 테스트 가능)
INSERT INTO member_report (reporter_id, reported_member_id, report_type, content, created_at, updated_at)
VALUES
    ('mem-001', 'mem-002', 'GENERAL',      '반복적으로 불쾌한 메시지를 보냈습니다.',                     DATE_SUB(@now, INTERVAL 30 MINUTE), DATE_SUB(@now, INTERVAL 30 MINUTE)),
    ('mem-001', 'mem-003', 'COMMENT',      '댓글에서 비방성 표현을 사용했습니다.',                       DATE_SUB(@now, INTERVAL 29 MINUTE), DATE_SUB(@now, INTERVAL 29 MINUTE)),
    ('mem-001', 'mem-002', 'BOOK_STORY',   '책 이야기 본문에 욕설이 포함되어 있습니다.',                 DATE_SUB(@now, INTERVAL 28 MINUTE), DATE_SUB(@now, INTERVAL 28 MINUTE)),
    ('mem-001', 'mem-003', 'CLUB_MEETING', '모임 채팅에서 지속적으로 분란을 유도했습니다.',             DATE_SUB(@now, INTERVAL 27 MINUTE), DATE_SUB(@now, INTERVAL 27 MINUTE)),
    ('mem-001', 'mem-002', 'GENERAL',      '개인정보를 요구하는 메시지를 반복 전송했습니다.',           DATE_SUB(@now, INTERVAL 26 MINUTE), DATE_SUB(@now, INTERVAL 26 MINUTE)),
    ('mem-001', 'mem-003', 'COMMENT',      '댓글 스팸이 반복되어 신고합니다.',                           DATE_SUB(@now, INTERVAL 25 MINUTE), DATE_SUB(@now, INTERVAL 25 MINUTE)),
    ('mem-001', 'mem-002', 'BOOK_STORY',   '책 이야기와 무관한 광고성 문구를 게시했습니다.',            DATE_SUB(@now, INTERVAL 24 MINUTE), DATE_SUB(@now, INTERVAL 24 MINUTE)),
    ('mem-001', 'mem-003', 'CLUB_MEETING', '모임 규칙 위반 발언이 누적되었습니다.',                     DATE_SUB(@now, INTERVAL 23 MINUTE), DATE_SUB(@now, INTERVAL 23 MINUTE)),
    ('mem-001', 'mem-002', 'GENERAL',      '상대방을 조롱하는 표현을 반복했습니다.',                     DATE_SUB(@now, INTERVAL 22 MINUTE), DATE_SUB(@now, INTERVAL 22 MINUTE)),
    ('mem-001', 'mem-003', 'COMMENT',      '댓글에서 타인을 공격하는 표현이 확인됩니다.',               DATE_SUB(@now, INTERVAL 21 MINUTE), DATE_SUB(@now, INTERVAL 21 MINUTE)),
    ('mem-001', 'mem-002', 'BOOK_STORY',   '책 이야기 댓글 유도성 도배를 진행했습니다.',                DATE_SUB(@now, INTERVAL 20 MINUTE), DATE_SUB(@now, INTERVAL 20 MINUTE)),
    ('mem-001', 'mem-003', 'CLUB_MEETING', '모임 진행을 방해하는 발언을 지속했습니다.',                 DATE_SUB(@now, INTERVAL 19 MINUTE), DATE_SUB(@now, INTERVAL 19 MINUTE)),
    ('mem-001', 'mem-002', 'GENERAL',      '반복된 괴롭힘성 언행으로 신고합니다.',                       DATE_SUB(@now, INTERVAL 18 MINUTE), DATE_SUB(@now, INTERVAL 18 MINUTE)),
    ('mem-001', 'mem-003', 'COMMENT',      '댓글에서 혐오 표현 사용이 있었습니다.',                     DATE_SUB(@now, INTERVAL 17 MINUTE), DATE_SUB(@now, INTERVAL 17 MINUTE)),
    ('mem-001', 'mem-002', 'BOOK_STORY',   '책 이야기 게시물에 부적절한 링크를 첨부했습니다.',          DATE_SUB(@now, INTERVAL 16 MINUTE), DATE_SUB(@now, INTERVAL 16 MINUTE)),
    ('mem-001', 'mem-003', 'CLUB_MEETING', '모임 공지와 무관한 선동성 내용이 반복되었습니다.',         DATE_SUB(@now, INTERVAL 15 MINUTE), DATE_SUB(@now, INTERVAL 15 MINUTE)),
    ('mem-001', 'mem-002', 'GENERAL',      '비매너 DM 발송 사례가 계속 발생했습니다.',                  DATE_SUB(@now, INTERVAL 14 MINUTE), DATE_SUB(@now, INTERVAL 14 MINUTE)),
    ('mem-001', 'mem-003', 'COMMENT',      '타 회원을 저격하는 댓글을 작성했습니다.',                   DATE_SUB(@now, INTERVAL 13 MINUTE), DATE_SUB(@now, INTERVAL 13 MINUTE)),
    ('mem-001', 'mem-002', 'BOOK_STORY',   '책 이야기에서 도배성 동일 문장을 반복했습니다.',            DATE_SUB(@now, INTERVAL 12 MINUTE), DATE_SUB(@now, INTERVAL 12 MINUTE)),
    ('mem-001', 'mem-003', 'CLUB_MEETING', '모임 참여자 대상 무례한 발언이 확인되었습니다.',            DATE_SUB(@now, INTERVAL 11 MINUTE), DATE_SUB(@now, INTERVAL 11 MINUTE)),
    ('mem-001', 'mem-002', 'GENERAL',      '커뮤니티 가이드라인 위반 내용이 누적되었습니다.',           DATE_SUB(@now, INTERVAL 10 MINUTE), DATE_SUB(@now, INTERVAL 10 MINUTE)),
    ('mem-001', 'mem-003', 'COMMENT',      '반복적 악성 댓글로 인해 신고합니다.',                       DATE_SUB(@now, INTERVAL 9 MINUTE),  DATE_SUB(@now, INTERVAL 9 MINUTE)),
    ('mem-001', 'mem-002', 'BOOK_STORY',   '비속어가 포함된 책 이야기 게시물을 작성했습니다.',          DATE_SUB(@now, INTERVAL 8 MINUTE),  DATE_SUB(@now, INTERVAL 8 MINUTE)),
    ('mem-001', 'mem-003', 'CLUB_MEETING', '모임 채널에서 욕설성 언행을 반복했습니다.',                 DATE_SUB(@now, INTERVAL 7 MINUTE),  DATE_SUB(@now, INTERVAL 7 MINUTE));

-- 다른 회원 신고 이력 (필터링 검증용)
INSERT INTO member_report (reporter_id, reported_member_id, report_type, content, created_at, updated_at)
VALUES
    ('mem-002', 'mem-001', 'GENERAL',      '개인 공격성 발언이 있어 신고합니다.',                       DATE_SUB(@now, INTERVAL 6 MINUTE),  DATE_SUB(@now, INTERVAL 6 MINUTE)),
    ('mem-003', 'mem-001', 'COMMENT',      '댓글로 불쾌감을 주는 표현이 있었습니다.',                   DATE_SUB(@now, INTERVAL 5 MINUTE),  DATE_SUB(@now, INTERVAL 5 MINUTE));
