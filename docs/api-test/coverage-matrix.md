# REST API 테스트 커버리지 매트릭스

## 기준

이 매트릭스는 `.omx/plans/endpoint-inventory-restassured-h2-api-tests-20260528T071303Z.md`의 REST 컨트롤러 인벤토리와 대조해 작성했다. WebSocket/STOMP 컨트롤러는 REST API 범위가 아니므로 제외 문서에 따로 기록했다.

상태 값은 다음 의미다.

- `완료`: 현재 테스트 코드가 존재한다.
- `예정`: 이번 API 테스트 확장 작업에서 추가해야 한다.
- `부분`: 일부 공통 보안/기반 검증으로만 확인됐고, 전용 도메인 테스트가 남아 있다.

검증 축은 `성공`, `401`, `403`, `400`, `도메인 실패` 중 해당되는 항목을 적용한다. 모든 인증 필요 API는 최소 401을 포함하고, 권한이나 소유권이 있는 API는 403을 포함한다.

## 전체 엔드포인트

| 컨트롤러 | 메서드 | 경로 | 상태 | 테스트를 추가하는 이유 |
| --- | --- | --- | --- | --- |
| HealthCheckController | GET | `/health` | 완료 | 인증 없이 서비스 상태를 확인하는 기준점이다. |
| AppVersionController | GET | `/api/v1/app/version` | 완료 | 앱 부팅/로그인 전 버전 정책 공개 조회, 플랫폼별 응답, 잘못된 platform, 정책 없음 실패를 검증한다. |
| AuthController | POST | `/api/v1/auth/email-verification` | 완료 | 회원가입 전 이메일 인증 요청의 성공, 이메일 형식 오류, 중복 요청을 검증한다. |
| AuthController | POST | `/api/v1/auth/email-verification/confirm` | 완료 | 인증 코드 일치, 만료, 불일치 상태를 검증한다. |
| AuthController | POST | `/api/v1/auth/signup` | 완료 | 가입 성공, 중복 이메일/닉네임, 검증 실패 payload를 검증한다. |
| AuthController | POST | `/api/v1/auth/login` | 완료 | 쿠키 발급 성공과 잘못된 자격 증명 실패를 검증한다. |
| AuthController | POST | `/api/v1/auth/app/login` | 완료 | `AuthApiTest`에서 `POST /api/v1/auth/app/login` 앱 전용 로그인 성공, 쿠키 발급, body refresh token 반환 계약을 검증한다. |
| AuthController | POST | `/api/v1/auth/app/refresh` | 완료 | `AuthApiTest`에서 `POST /api/v1/auth/app/refresh`의 `X-Refresh-Token` 기반 앱 토큰 재발급, refresh token 회전, 재사용 실패를 검증한다. |
| AuthController | POST | `/api/v1/auth/logout` | 완료 | JWT 쿠키 기반 로그아웃과 인증 누락 처리를 검증한다. |
| AuthController | POST | `/api/v1/auth/app/logout` | 완료 | `AuthApiTest`에서 `POST /api/v1/auth/app/logout`의 `X-Refresh-Token` 기반 앱 로그아웃과 이후 refresh token 무효화를 검증한다. |
| AuthController | POST | `/api/v1/auth/temp-password` | 완료 | 임시 비밀번호 발급, 미존재 이메일, 메일 경계 더블을 검증한다. |
| BookController | GET | `/api/v1/books/search` | 완료 | 알라딘 검색 경계의 deterministic 응답과 공개 조회 계약을 검증한다. |
| BookController | GET | `/api/v1/books/{isbn}` | 완료 | 도서 상세 조회 성공과 외부 조회 실패를 검증한다. |
| BookController | GET | `/api/v1/books/recommend` | 완료 | 추천 도서 조회와 캐시/외부 경계 fallback을 검증한다. |
| BookController | POST | `/api/v1/books/{isbn}/like` | 완료 | 인증 사용자 좋아요 토글, 401, 중복/해제 상태를 검증한다. |
| BookController | GET | `/api/v1/books/me/likes` | 완료 | 내 좋아요 목록의 인증 필요 계약을 검증한다. |
| BookController | GET | `/api/v1/books/{memberNickname}/likes` | 완료 | 다른 회원 좋아요 목록 공개 조회와 대상 없음 실패를 검증한다. |
| BookStoryAdminController | GET | `/api/v1/admin/book-stories` | 완료 | 관리자 목록 조회, 익명 401, 비관리자 403을 검증한다. |
| BookStoryAdminController | GET | `/api/v1/admin/book-stories/{bookStoryId}` | 완료 | 관리자 상세 조회와 not found를 검증한다. |
| BookStoryAdminController | DELETE | `/api/v1/admin/book-stories/{bookStoryId}` | 완료 | 관리자 삭제 권한과 not found를 검증한다. |
| BookStoryAdminController | DELETE | `/api/v1/admin/book-stories/{bookStoryId}/comments/{commentId}` | 완료 | 관리자 댓글 삭제 권한과 댓글 없음 실패를 검증한다. |
| BookStoryAdminController | GET | `/api/v1/admin/book-stories/members/{memberNickname}` | 완료 | 특정 회원 글 관리자 조회와 회원 없음 실패를 검증한다. |
| BookStoryController | POST | `/api/v1/book-stories` | 완료 | 글 작성 성공, 401, 403, validation을 검증한다. |
| BookStoryController | GET | `/api/v1/book-stories` | 완료 | 공개 목록 조회와 페이징 파라미터를 검증한다. |
| BookStoryController | GET | `/api/v1/book-stories/me` | 완료 | 내 글 목록의 인증 필요 계약을 검증한다. |
| BookStoryController | GET | `/api/v1/book-stories/following` | 완료 | 팔로잉 글 목록의 인증 필요 계약을 검증한다. |
| BookStoryController | GET | `/api/v1/book-stories/members/{nickname}` | 완료 | 회원별 글 조회와 대상 없음 실패를 검증한다. |
| BookStoryController | GET | `/api/v1/book-stories/clubs/{clubId}` | 완료 | 클럽별 글 조회와 클럽 없음 실패를 검증한다. |
| BookStoryController | GET | `/api/v1/book-stories/{bookStoryId}` | 완료 | 글 상세 조회와 not found를 검증한다. |
| BookStoryController | POST | `/api/v1/book-stories/{bookStoryId}/like` | 완료 | 좋아요 토글, 401, 대상 없음 실패를 검증한다. |
| BookStoryController | PATCH | `/api/v1/book-stories/{bookStoryId}` | 완료 | 작성자 수정, 소유권 403, validation을 검증한다. |
| BookStoryController | DELETE | `/api/v1/book-stories/{bookStoryId}` | 완료 | 작성자 삭제, 소유권 403, not found를 검증한다. |
| BookStoryController | POST | `/api/v1/book-stories/{bookStoryId}/comments` | 완료 | 댓글 작성, 401, validation을 검증한다. |
| BookStoryController | PATCH | `/api/v1/book-stories/{bookStoryId}/comments/{commentId}` | 완료 | 댓글 수정 권한과 댓글 없음 실패를 검증한다. |
| BookStoryController | DELETE | `/api/v1/book-stories/{bookStoryId}/comments/{commentId}` | 완료 | 댓글 삭제 권한과 댓글 없음 실패를 검증한다. |
| BookStoryController | GET | `/api/v1/book-stories/search/{bookId}` | 완료 | 도서 기준 글 검색과 페이징을 검증한다. |
| ClubAdminController | GET | `/api/v1/admin/clubs` | 완료 | 관리자 클럽 목록, 익명 401, 비관리자 403을 검증한다. |
| ClubAdminController | GET | `/api/v1/admin/clubs/{clubId}` | 완료 | 관리자 클럽 상세와 not found를 검증한다. |
| ClubAdminController | PUT | `/api/v1/admin/clubs/{clubId}` | 완료 | 관리자 클럽 수정, validation, not found를 검증한다. |
| ClubAdminController | GET | `/api/v1/admin/clubs/{clubId}/active-members` | 완료 | 관리자 활성 멤버 조회와 클럽 없음 실패를 검증한다. |
| ClubAdminController | GET | `/api/v1/admin/clubs/members/{memberNickname}` | 완료 | 회원별 클럽 관리자 조회와 회원 없음 실패를 검증한다. |
| ClubController | GET | `/api/v1/clubs/check-name` | 완료 | 클럽명 중복 확인과 query validation을 검증한다. |
| ClubController | POST | `/api/v1/clubs` | 완료 | 클럽 생성, 401, 403, validation을 검증한다. |
| ClubController | PUT | `/api/v1/clubs/{clubId}` | 완료 | 운영자 수정 권한, 비운영자 403, validation을 검증한다. |
| ClubController | GET | `/api/v1/clubs/{clubId}` | 완료 | 클럽 상세 조회와 not found를 검증한다. |
| ClubController | DELETE | `/api/v1/clubs/{clubId}` | 완료 | 운영자 삭제 권한과 비운영자 403을 검증한다. |
| ClubController | GET | `/api/v1/clubs/search` | 완료 | 공개 검색과 검색 파라미터를 검증한다. |
| ClubController | GET | `/api/v1/clubs/recommendations` | 완료 | 개인화 추천의 인증 필요 계약을 검증한다. |
| ClubController | GET | `/api/v1/clubs/{clubId}/home` | 완료 | 클럽 홈 공개 조회와 not found를 검증한다. |
| ClubController | POST | `/api/v1/clubs/{clubId}/join` | 완료 | 가입 성공, 중복 가입, 정원/상태 실패를 검증한다. |
| ClubController | GET | `/api/v1/clubs/{clubId}/me` | 완료 | 내 클럽 멤버십 상태 조회와 401을 검증한다. |
| ClubController | GET | `/api/v1/clubs/{clubId}/members` | 완료 | 클럽 멤버 목록과 비멤버/클럽 없음 실패를 검증한다. |
| ClubController | PATCH | `/api/v1/clubs/{clubId}/members/{clubMemberId}` | 완료 | 멤버 역할/상태 변경 권한과 validation을 검증한다. |
| ClubController | DELETE | `/api/v1/clubs/{clubId}/leave` | 완료 | 탈퇴 성공, 운영자 탈퇴 제한, 비멤버 실패를 검증한다. |
| ClubController | GET | `/api/v1/clubs` | 완료 | 클럽 목록 공개 조회와 페이징을 검증한다. |
| MyClubController | GET | `/api/v1/me/clubs` | 완료 | 내 클럽 목록의 인증 필요 계약을 검증한다. |
| ClubBookshelfController | GET | `/api/v1/clubs/{clubId}/bookshelves` | 완료 | 책장 목록 조회와 클럽 접근 권한을 검증한다. |
| ClubBookshelfController | GET | `/api/v1/clubs/{clubId}/bookshelves/{meetingId}` | 완료 | 모임 책장 상세와 not found를 검증한다. |
| ClubBookshelfController | POST | `/api/v1/clubs/{clubId}/bookshelves` | 완료 | 책장 생성 권한, validation, 중복/상태 실패를 검증한다. |
| ClubBookshelfController | GET | `/api/v1/clubs/{clubId}/bookshelves/{meetingId}/edit` | 완료 | 수정 화면 데이터 조회 권한을 검증한다. |
| ClubBookshelfController | PATCH | `/api/v1/clubs/{clubId}/bookshelves/{meetingId}` | 완료 | 책장 수정 권한, validation, not found를 검증한다. |
| ClubBookshelfController | DELETE | `/api/v1/clubs/{clubId}/bookshelves/{meetingId}` | 완료 | 책장 삭제 권한과 not found를 검증한다. |
| ClubBookshelfController | GET | `/api/v1/clubs/{clubId}/bookshelves/{meetingId}/topics` | 완료 | 토론 주제 목록 조회와 접근 권한을 검증한다. |
| ClubBookshelfController | POST | `/api/v1/clubs/{clubId}/bookshelves/{meetingId}/topics` | 완료 | 토론 주제 생성, validation, 권한 실패를 검증한다. |
| ClubBookshelfController | PATCH | `/api/v1/clubs/{clubId}/bookshelves/{meetingId}/topics/{topicId}` | 완료 | 토론 주제 수정 권한과 not found를 검증한다. |
| ClubBookshelfController | DELETE | `/api/v1/clubs/{clubId}/bookshelves/{meetingId}/topics/{topicId}` | 완료 | 토론 주제 삭제 권한과 not found를 검증한다. |
| ClubBookshelfController | GET | `/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews` | 완료 | 후기 목록 조회와 접근 권한을 검증한다. |
| ClubBookshelfController | POST | `/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews` | 완료 | 후기 작성, validation, 중복/권한 실패를 검증한다. |
| ClubBookshelfController | PATCH | `/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews/{reviewId}` | 완료 | 후기 수정 권한과 not found를 검증한다. |
| ClubBookshelfController | DELETE | `/api/v1/clubs/{clubId}/bookshelves/{meetingId}/reviews/{reviewId}` | 완료 | 후기 삭제 권한과 not found를 검증한다. |
| ClubMeetingController | GET | `/api/v1/clubs/{clubId}/meetings/next` | 완료 | 다음 모임 조회와 클럽 접근 권한을 검증한다. |
| ClubMeetingController | GET | `/api/v1/clubs/{clubId}/meetings/{meetingId}` | 완료 | 모임 상세와 not found를 검증한다. |
| ClubMeetingController | GET | `/api/v1/clubs/{clubId}/meetings/{meetingId}/members` | 완료 | 모임 멤버 조회와 권한 실패를 검증한다. |
| ClubMeetingController | PUT | `/api/v1/clubs/{clubId}/meetings/{meetingId}/teams` | 완료 | 팀 편성 권한, validation, 상태 실패를 검증한다. |
| ClubMeetingController | GET | `/api/v1/clubs/{clubId}/meetings/{meetingId}/teams/{teamId}/topics` | 완료 | 팀별 주제 조회와 접근 권한을 검증한다. |
| ClubNoticeController | GET | `/api/v1/clubs/{clubId}/notices/latest` | 완료 | 최신 공지 공개 조회와 클럽 없음 실패를 검증한다. |
| ClubNoticeController | GET | `/api/v1/clubs/{clubId}/notices` | 완료 | 공지 목록 조회와 접근 권한을 검증한다. |
| ClubNoticeController | GET | `/api/v1/clubs/{clubId}/notices/{noticeId}` | 완료 | 공지 상세와 not found를 검증한다. |
| ClubNoticeController | POST | `/api/v1/clubs/{clubId}/notices` | 완료 | 공지 작성 권한, validation, 403을 검증한다. |
| ClubNoticeController | PATCH | `/api/v1/clubs/{clubId}/notices/{noticeId}` | 완료 | 공지 수정 권한과 not found를 검증한다. |
| ClubNoticeController | DELETE | `/api/v1/clubs/{clubId}/notices/{noticeId}` | 완료 | 공지 삭제 권한과 not found를 검증한다. |
| ClubNoticeController | POST | `/api/v1/clubs/{clubId}/notices/{noticeId}/votes/{voteId}` | 완료 | 투표 성공, 중복/마감 상태 실패를 검증한다. |
| ClubNoticeController | GET | `/api/v1/clubs/{clubId}/notices/{noticeId}/comments` | 완료 | 공지 댓글 목록 조회와 접근 권한을 검증한다. |
| ClubNoticeController | POST | `/api/v1/clubs/{clubId}/notices/{noticeId}/comments` | 완료 | 공지 댓글 작성, validation, 401을 검증한다. |
| ClubNoticeController | PATCH | `/api/v1/clubs/{clubId}/notices/{noticeId}/comments/{commentId}` | 완료 | 댓글 수정 권한과 not found를 검증한다. |
| ClubNoticeController | DELETE | `/api/v1/clubs/{clubId}/notices/{noticeId}/comments/{commentId}` | 완료 | 댓글 삭제 권한과 not found를 검증한다. |
| S3Controller | POST | `/api/v1/image/{type}/upload-url` | 완료 | 이미지 업로드 URL 발급, 파일 형식 validation, 프로필 미완료 403을 검증한다. |
| MemberAdminController | GET | `/api/v1/admin/members/emails` | 완료 | 관리자 이메일 검색, 익명 401, 비관리자 403을 검증한다. |
| MemberAdminController | GET | `/api/v1/admin/members` | 완료 | 관리자 회원 목록과 검색/페이징을 검증한다. |
| MemberAdminController | GET | `/api/v1/admin/members/{memberNickName}` | 완료 | 관리자 회원 상세와 회원 없음 실패를 검증한다. |
| MemberController | POST | `/api/v1/members/additional-info` | 완료 | 프로필 추가 정보 입력과 validation, 401을 검증한다. |
| MemberController | POST | `/api/v1/members/check-nickname` | 완료 | 닉네임 중복 확인과 query validation을 검증한다. |
| MemberController | POST | `/api/v1/members/{memberNickname}/following` | 완료 | 팔로우 성공, 자기 자신/중복/대상 없음 실패를 검증한다. |
| MemberController | DELETE | `/api/v1/members/{memberNickname}/following` | 완료 | 언팔로우 성공과 팔로우 관계 없음 실패를 검증한다. |
| MemberController | DELETE | `/api/v1/members/{memberNickname}/follower` | 완료 | 팔로워 삭제 권한과 관계 없음 실패를 검증한다. |
| MemberController | POST | `/api/v1/members/{memberNickname}/block` | 완료 | 차단 성공, 자기 자신 차단 실패, 대상 없음 실패를 검증한다. |
| MemberController | DELETE | `/api/v1/members/{memberNickname}/block` | 완료 | 차단 해제 성공과 차단 관계 없음 실패를 검증한다. |
| MemberController | GET | `/api/v1/members/me/following` | 완료 | 내 팔로잉 목록 인증 계약과 페이징을 검증한다. |
| MemberController | GET | `/api/v1/members/me/follower` | 완료 | 내 팔로워 목록 인증 계약과 페이징을 검증한다. |
| MemberController | GET | `/api/v1/members/me/blocks` | 완료 | 내 차단 목록 인증 계약과 페이징을 검증한다. |
| MemberController | GET | `/api/v1/members/{memberNickname}/followings` | 완료 | 다른 회원 팔로잉 목록과 회원 없음 실패를 검증한다. |
| MemberController | GET | `/api/v1/members/{memberNickname}/followers` | 완료 | 다른 회원 팔로워 목록과 회원 없음 실패를 검증한다. |
| MemberController | PATCH | `/api/v1/members/me` | 완료 | 내 프로필 수정, validation, 프로필 미완료 403을 검증한다. |
| MemberController | GET | `/api/v1/members/me` | 완료 | 내 프로필 조회, 401, 프로필 미완료 403을 검증한다. |
| MemberController | GET | `/api/v1/members/me/follow-count` | 완료 | 기반 테스트에서 200/401/403을 확인했고, 도메인 전용 검증은 회원 배치에서 보강한다. |
| MemberController | GET | `/api/v1/members/{memberNickname}` | 완료 | 다른 회원 프로필 조회와 회원 없음 실패를 검증한다. |
| MemberController | POST | `/api/v1/members/find-email` | 완료 | 이메일 찾기 성공, 미존재 정보, validation을 검증한다. |
| MemberController | PATCH | `/api/v1/members/me/update-password` | 완료 | 비밀번호 변경 성공, 현재 비밀번호 불일치, validation을 검증한다. |
| MemberController | PATCH | `/api/v1/members/me/update-email` | 완료 | 이메일 변경 성공, 인증 코드 실패, 중복 이메일을 검증한다. |
| MemberController | GET | `/api/v1/members/me/login-status` | 완료 | 로그인 상태 응답과 쿠키 없음 상태를 검증한다. |
| MemberController | GET | `/api/v1/members/me/recommend` | 완료 | 회원 추천 목록과 인증 필요 계약을 검증한다. |
| MemberController | POST | `/api/v1/members/withdrawal` | 완료 | 회원 탈퇴 성공, 재인증/상태 실패를 검증한다. |
| NewsAdminController | GET | `/api/v1/admin/news` | 완료 | 관리자 뉴스 목록, 익명 401, 비관리자 403을 검증한다. |
| NewsAdminController | GET | `/api/v1/admin/news/{newsId}` | 완료 | 관리자 뉴스 상세와 not found를 검증한다. |
| NewsAdminController | POST | `/api/v1/admin/news` | 완료 | 관리자 뉴스 생성, validation, 권한 실패를 검증한다. |
| NewsAdminController | PATCH | `/api/v1/admin/news/{newsId}` | 완료 | 관리자 뉴스 수정, validation, not found를 검증한다. |
| NewsAdminController | DELETE | `/api/v1/admin/news/{newsId}` | 완료 | 관리자 뉴스 삭제와 not found를 검증한다. |
| NewsAdminController | GET | `/api/v1/admin/news/members/{memberNickname}` | 완료 | 회원별 뉴스 관리자 조회와 회원 없음 실패를 검증한다. |
| NewsController | GET | `/api/v1/news` | 완료 | 공개 뉴스 목록과 페이징을 검증한다. |
| NewsController | GET | `/api/v1/news/me` | 완료 | 내 뉴스 목록의 인증 필요 계약을 검증한다. |
| NewsController | GET | `/api/v1/news/{newsId}` | 완료 | 뉴스 상세 공개 조회와 not found를 검증한다. |
| NotificationController | GET | `/api/v1/notifications` | 완료 | 알림 목록 인증 계약과 페이징을 검증한다. |
| NotificationController | GET | `/api/v1/notifications/preview` | 완료 | 알림 미리보기 인증 계약을 검증한다. |
| NotificationController | PATCH | `/api/v1/notifications/{notificationId}/read` | 완료 | 읽음 처리 성공, 소유권 403, not found를 검증한다. |
| NotificationController | GET | `/api/v1/notifications/settings` | 완료 | 알림 설정 조회 인증 계약을 검증한다. |
| NotificationController | PATCH | `/api/v1/notifications/settings/{settingType}` | 완료 | 알림 설정 변경, enum validation, 인증 실패를 검증한다. |
| ChatHistoryController | GET | `/api/v1/clubs/{clubId}/meetings/{meetingId}/teams/{teamId}/chat/messages` | 완료 | WebSocket이 아닌 REST 채팅 히스토리 조회이므로 접근 권한과 페이징을 검증한다. |
| ReportController | POST | `/api/v1/reports` | 완료 | 신고 생성, validation, 중복/대상 없음 실패를 검증한다. |
| ReportController | GET | `/api/v1/reports/me` | 완료 | 내 신고 목록의 인증 필요 계약을 검증한다. |
