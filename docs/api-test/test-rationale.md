# API 테스트별 추가 이유

## 문서 기준

이 문서는 현재 추가된 RestAssured API 테스트가 왜 필요한지 테스트 메서드 단위로 설명한다. 세부 엔드포인트별 상태는 `coverage-matrix.md`를 기준으로 보고, 여기서는 테스트가 묶인 이유와 검증 의도를 기록한다.

## ApiTestInfrastructureTest

| 테스트 | 추가 이유 |
| --- | --- |
| `testProfileUsesH2AndDoesNotIncludeProductionProfiles` | API 테스트가 운영 프로파일, 운영 DB, Redis, S3, 메일 설정을 끌고 오지 않는지 먼저 고정해야 이후 실패 원인을 API 계약으로 해석할 수 있다. |
| `healthEndpointIsCallableWithRestAssured` | `RANDOM_PORT` 서버와 RestAssured 연결이 실제 HTTP 경로로 동작하는지 확인하는 가장 작은 기준점이다. |
| `authenticatedFixtureCanCallProfileCompletedApi` | 완료 프로필 JWT fixture가 실제 보안 필터를 통과하는지 확인한다. |
| `anonymousRequestToAuthenticatedApiReturnsUnauthorized` | 인증 쿠키가 없는 요청이 401로 막히는 공통 보안 계약을 고정한다. |
| `incompleteProfileFixtureIsBlockedBeforeProtectedApiExecution` | 프로필 미완료 사용자가 보호 API에서 403으로 차단되는 전역 필터 계약을 고정한다. |
| `adminFixtureCanIssueJwtCookies` | 관리자 API 배치를 만들기 전에 `ROLE_ADMIN` JWT fixture가 정상 생성되는지 확인한다. |

## AuthApiTest

| 테스트 | 추가 이유 |
| --- | --- |
| `sendEmailVerificationSucceedsWithMailBoundaryMocked` | 실제 메일 발송 없이 이메일 인증 요청의 HTTP 성공 계약을 검증한다. |
| `sendEmailVerificationRejectsInvalidEmail` | 이메일 형식 validation이 컨트롤러 진입 시 400으로 반환되는지 검증한다. |
| `confirmEmailVerificationSucceedsWhenCodeMatches` | Redis 더블에 저장된 인증 코드와 요청 코드가 일치할 때 인증 완료 흐름을 검증한다. |
| `confirmEmailVerificationRejectsWrongCode` | 잘못된 인증 코드가 가입 가능 상태로 넘어가지 않도록 실패 계약을 검증한다. |
| `signUpSucceedsAfterEmailVerificationAndSetsJwtCookies` | 인증 완료 후 회원가입, AuthUser/Member 저장, JWT 쿠키 발급을 한 번에 검증한다. |
| `signUpRejectsUnverifiedEmail` | 이메일 인증을 건너뛴 가입 시도를 차단한다. |
| `signUpRejectsDuplicateEmail` | 중복 이메일 가입이 DB 제약과 서비스 검증으로 실패하는지 확인한다. |
| `signUpRejectsInvalidPayload` | 가입 요청 body의 필수값/형식 validation을 고정한다. |
| `loginSucceedsAndSetsJwtCookies` | 저장된 사용자 자격 증명으로 로그인하고 access/refresh 쿠키가 내려오는지 검증한다. |
| `loginRejectsWrongPassword` | 비밀번호 불일치가 인증 성공으로 오인되지 않도록 실패 계약을 검증한다. |
| `logoutClearsJwtCookiesWhenTokensExist` | JWT 쿠키 기반 로그아웃과 토큰 캐시 경계 호출 후 응답 쿠키 정리를 검증한다. |
| `sendTempPasswordSucceedsForKnownEmail` | 가입된 이메일에 대한 임시 비밀번호 발급 HTTP 계약을 검증한다. |
| `sendTempPasswordReturnsNotFoundForUnknownEmail` | 미가입 이메일에 대한 정보 노출 없는 실패 계약을 검증한다. |
| `sendTempPasswordRejectsInvalidEmail` | 임시 비밀번호 요청의 이메일 validation을 고정한다. |

## PublicMemberApiTest

| 테스트 | 추가 이유 |
| --- | --- |
| `checkNicknameReturnsDuplicatedState` | 공개 닉네임 중복 확인 API가 DB 상태를 반영하는지 검증한다. |
| `checkNicknameRejectsInvalidNickname` | query parameter validation이 누락되지 않도록 고정한다. |
| `findEmailSucceedsForMatchingNameAndPhoneNumber` | 이름/전화번호 조합으로 이메일 찾기가 성공하는 기본 사용자 복구 흐름을 검증한다. |
| `findEmailReturnsNotFoundForUnknownPersonalInfo` | 일치하는 회원이 없을 때 404 계약을 검증한다. |
| `findEmailRejectsInvalidPayload` | 이메일 찾기 body validation을 고정한다. |
| `protectedMemberApiReturnsUnauthorizedWithoutCookie` | 공개 회원 API와 보호 회원 API의 보안 경계가 섞이지 않도록 401을 확인한다. |

## MemberApiTest

| 테스트 | 추가 이유 |
| --- | --- |
| `additionalInfoCompletesIncompleteProfile` | 미완료 사용자가 허용된 추가 정보 API를 통해 완료 상태로 전환되는 핵심 온보딩 흐름을 검증한다. |
| `profileReadAndUpdateSucceedForCompletedUser` | 완료 사용자의 내 프로필 조회/수정 성공 계약을 검증한다. |
| `incompleteProfileCannotReadProtectedProfile` | 프로필 완료 필터가 보호 프로필 API를 사전에 차단하는지 검증한다. |
| `followListAndUnfollowFlowSucceeds` | 팔로우 생성, 목록 조회, 언팔로우의 상태 전이를 검증한다. |
| `selfFollowIsRejected` | 자기 자신 팔로우 금지 도메인 규칙을 검증한다. |
| `followerDeleteFlowSucceeds` | 팔로워 삭제 API가 관계의 반대 방향을 올바르게 처리하는지 검증한다. |
| `blockListAndUnblockFlowSucceeds` | 차단 생성, 목록 조회, 해제의 상태 전이를 검증한다. |
| `selfBlockIsRejected` | 자기 자신 차단 금지 도메인 규칙을 검증한다. |
| `otherProfileAndFollowListsAreReadable` | 다른 회원 공개 프로필과 팔로우 목록 조회 계약을 검증한다. |
| `updatePasswordSucceedsAndRejectsWrongCurrentPassword` | 현재 비밀번호 확인과 새 비밀번호 반영 실패/성공 경계를 검증한다. |
| `updateEmailSucceedsAfterVerification` | 이메일 인증 코드 확인 후 이메일 변경이 가능한지 검증한다. |
| `loginStatusRecommendationFollowCountAndWithdrawalSucceed` | 로그인 상태, 회원 추천, 팔로우 수, 탈퇴처럼 회원 화면에서 반복 호출되는 단건 API들을 함께 고정한다. |

## ClubMeetingNoticeApiTest

| 테스트 | 추가 이유 |
| --- | --- |
| `clubManagementFlowCoversClubEndpoints` | 클럽 생성부터 가입, 멤버 관리, 수정, 삭제까지 클럽 관리 API의 대표 상태 전이를 검증한다. |
| `bookshelfMeetingTopicReviewAndChatFlowCoversRestEndpoints` | 책장, 모임, 발제, 한줄평, 팀 편성, REST 채팅 히스토리 조회를 하나의 모임 흐름으로 검증한다. |
| `noticeVoteAndCommentFlowCoversNoticeEndpoints` | 공지 작성, 투표, 댓글, 수정, 삭제가 클럽 권한과 함께 동작하는지 검증한다. |
| `nonMemberCannotAccessMemberOnlyClubResources` | 클럽 멤버 전용 REST API가 비멤버 접근을 숨김/거부하는 계약을 검증한다. |
| `authenticatedClubEndpointRequiresCookie` | 클럽 생성 같은 인증 필수 API가 익명 요청을 401로 차단하는지 검증한다. |

## BookStoryNewsReportNotificationImageApiTest

| 테스트 | 추가 이유 |
| --- | --- |
| `bookSearchDetailRecommendLikeAndLikedListsSucceed` | 알라딘 경계 더블, 추천 캐시 fallback, 도서 좋아요 토글, 내/타인 좋아요 목록, 401을 한 번에 검증한다. |
| `bookStoryCrudListsLikeCommentAndFailuresSucceed` | 책 이야기 작성, 목록, 상세, 좋아요, 댓글, 수정/삭제, 소유권 403, validation을 실제 H2 상태 전이로 검증한다. |
| `newsListMyListAndDetailUseH2Rows` | 공개 뉴스, 내 뉴스, 상세 조회가 H2에 저장된 게시 기간과 요청자 이메일을 기준으로 동작하는지 검증한다. |
| `reportCreateListValidationAndBusinessFailuresSucceed` | 신고 생성, 내 신고 목록, 자기 자신 신고 금지, validation, 401을 검증한다. |
| `notificationListPreviewReadAndSettingsSucceed` | 알림 목록/미리보기, 읽음 처리, 알림 설정 조회/토글, enum 오류, 401을 검증한다. |
| `imageUploadUrlValidatesAuthenticationProfileAndFileType` | S3 네트워크 없이 presigned URL 발급 성공, 파일 형식 실패, 익명 401, 프로필 미완료 403을 검증한다. |

## AdminApiTest

| 테스트 | 추가 이유 |
| --- | --- |
| `memberAdminEndpointsRequireAdminAndReturnMemberData` | 관리자 회원 이메일 검색, 회원 목록, 회원 상세, 회원 없음 404, 익명 401, 비관리자 403을 검증한다. |
| `clubAdminEndpointsCoverListDetailUpdateMembersAndSecurity` | 관리자 클럽 목록, 상세, 수정, 활성 멤버 조회, 회원별 클럽 조회, validation, not found, 401/403을 검증한다. |
| `newsAdminEndpointsCoverCrudMemberLookupValidationAndSecurity` | 관리자 뉴스 생성, 목록, 상세, 수정, 회원별 조회, validation, not found, 삭제, 401/403을 검증한다. |
| `bookStoryAdminEndpointsCoverListDetailDeleteCommentMemberLookupAndSecurity` | 관리자 책 이야기 목록, 상세, 회원별 조회, 댓글 삭제, 글 삭제, not found, 401/403을 검증한다. |
