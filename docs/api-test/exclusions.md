# API 테스트 제외 범위

## 제외 원칙

이번 테스트 범위는 HTTP REST API다. 컨트롤러 매핑이 존재하는 REST 엔드포인트는 인증/인가가 필요하더라도 테스트 대상에 포함한다. 반대로 프로토콜, 외부 시스템, 운영 인프라 자체를 검증해야 하는 항목은 API 테스트에서 제외하고 테스트 더블 또는 별도 테스트 계층으로 분리한다.

## 제외 항목

| 제외 항목 | 제외 이유 | 대체 검증 |
| --- | --- | --- |
| WebSocket/STOMP 실시간 연결 | RestAssured는 HTTP REST 요청/응답 검증 도구이고, STOMP 세션·구독·브로커 흐름은 테스트 방식이 다르다. | 별도 WebSocket/STOMP 통합 테스트에서 handshake, subscribe, publish를 검증한다. |
| 실제 Redis 서버 연결 | API 테스트가 로컬 Redis 실행 여부에 의존하면 재현성이 떨어지고 CI에서 불안정해진다. | `TokenCacheService`, `RedisTemplate`, `StringRedisTemplate`을 테스트 더블로 둔다. |
| 실제 메일 발송 | 테스트 실행 중 외부 SMTP로 메일을 보내면 비용, 속도, 계정 보안 문제가 생긴다. | 메일 서비스 테스트 더블을 사용하고, API는 요청/응답 및 이벤트 발생 경로만 검증한다. |
| 실제 S3 presigned URL 생성/삭제 네트워크 호출 | AWS 자격 증명과 네트워크가 필요하며 API 동작 테스트의 관심사가 아니다. | `S3Service` 테스트 더블로 성공/실패 응답을 고정한다. |
| 실제 알라딘 API 호출 | 외부 API 상태, rate limit, 네트워크에 따라 테스트 결과가 흔들린다. | 알라딘 호출 경계인 `RestTemplate` 또는 서비스 결과를 deterministic fixture로 고정한다. |
| OAuth2 제공자 실제 로그인 플로우 | 구글/카카오/네이버 인증 화면과 콜백은 외부 인증 시스템 E2E에 가깝다. | 테스트 프로파일에는 더미 OAuth2 registration만 둔다. 자체 auth REST API와 JWT 필터 체인은 테스트한다. |
| Flyway 마이그레이션 호환성 검증 | 운영 MySQL 마이그레이션 검증은 API 요청/응답 검증과 관심사가 다르다. | API 테스트는 Hibernate `create-drop`을 사용한다. 마이그레이션은 별도 DB 호환성 테스트에서 검증한다. |

## 제외하지 않는 항목

- 인증이 필요한 REST API
- 관리자 REST API
- REST 방식 채팅 히스토리 조회 API
- 프로필 미완료 사용자 403
- 비관리자 403
- 잘못된 요청 400
- 도메인 상태 오류와 not found 계열 실패

즉, "외부 시스템 자체"는 제외하지만, 그 외부 시스템을 사용하는 REST API의 HTTP 계약은 테스트 더블을 통해 검증한다.
