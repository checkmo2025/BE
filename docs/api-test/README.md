# RestAssured + H2 API 테스트 가이드

## 목적

이 문서는 Checkmo의 HTTP REST API를 실제 Spring Boot 서버(`RANDOM_PORT`)로 띄운 뒤 RestAssured로 호출하는 통합 테스트 기준을 정리한다. 테스트 데이터베이스는 H2 인메모리 DB를 사용하고, 인증/인가가 필요한 API도 실제 JWT 필터 체인을 통과하도록 fixture를 만든다.

## 실행 명령

```bash
./gradlew test --tests checkmo.support.ApiTestInfrastructureTest
```

전체 API 테스트가 확장된 뒤에는 다음 명령을 기준으로 검증한다.

```bash
./gradlew test --tests '*Api*'
./gradlew test
```

## 테스트 프로파일

- `src/test/resources/application.yml`이 메인 `application.yml`을 테스트 classpath에서 가린다.
- `test` 프로파일만 활성화한다.
- 운영 프로파일인 `prod`, `redis`, `aladin`, `mail`, `jwt`, `oauth2`, `s3`는 테스트 active profile에 포함하지 않는다.
- H2는 `MODE=MySQL`로 실행해 MySQL 문법과 최대한 가깝게 동작시킨다.

## 테스트 메타 어노테이션

API 테스트의 공통 스프링 컨텍스트 설정은 `@ApiTest`에 모았다.

- `@Tag("integration")`: JUnit 태그로 통합 테스트를 식별한다.
- `@ActiveProfiles("test")`: H2와 테스트용 더미 설정만 사용한다.
- `@SpringBootTest(webEnvironment = RANDOM_PORT)`: 실제 HTTP 서버를 띄우고 RestAssured로 호출한다.
- `@TestConstructor(autowireMode = ALL)`: 이후 생성자 주입 방식의 테스트를 작성할 수 있게 열어 둔다.

`ApiTestSupport`는 `@ApiTest`를 사용하면서 RestAssured 포트 설정, H2 데이터 cleanup, JWT fixture, 외부 경계 mock을 담당한다. 따라서 새 REST API 테스트는 기본적으로 `ApiTestSupport`를 상속한다.

HTTP 서버가 필요 없는 일반 스프링 컨텍스트 통합 테스트에는 별도의 `@SpringTest`를 사용한다. `@SpringTest`는 같은 `test` 프로파일과 `integration` 태그를 쓰지만 `webEnvironment = NONE`으로 컨텍스트를 띄운다. 이 둘을 분리한 이유는 REST API 테스트와 일반 스프링 통합 테스트의 실행 비용과 목적이 다르기 때문이다.

## 스키마 전략

현재 API 테스트는 Flyway를 끄고 Hibernate `create-drop`으로 스키마를 만든다.

이 선택은 API 동작 검증을 우선하기 위한 것이다. 기존 Flyway 마이그레이션은 운영 MySQL을 기준으로 작성되어 있고, H2 호환성 문제를 API 테스트의 선행 조건으로 만들면 테스트 목적이 "REST API 동작"에서 "마이그레이션 호환성"으로 흔들린다. Flyway 마이그레이션 검증은 별도 MySQL/Testcontainers 계층에서 다루는 것이 맞다.

## 외부 경계 처리

테스트 기반 클래스 `ApiTestSupport`는 다음 경계를 테스트 더블로 고정한다.

- Redis 토큰 캐시와 RedisTemplate 계열 빈
- 알라딘 호출용 RestTemplate
- 메일 발송 서비스
- S3 서비스
- ApplicationReadyEvent 또는 `@Scheduled`로 실행될 수 있는 스케줄러

이렇게 하는 이유는 API 테스트가 네트워크, 운영 자격 증명, 로컬 Redis/MySQL 실행 여부와 무관하게 항상 같은 결과를 내야 하기 때문이다.

## 인증 fixture

`ApiTestSupport`는 다음 사용자를 만들 수 있어야 한다.

- 익명 사용자: 쿠키 없음, 인증 필요 API에서 401 확인
- 일반 완료 사용자: `ROLE_USER`, `profileCompleted=true`
- 일반 미완료 사용자: `ROLE_USER`, `profileCompleted=false`, 보호 API에서 403 확인
- 관리자 사용자: `ROLE_ADMIN`, 관리자 API의 성공/403 비교에 사용

현재 기반 검증은 `ApiTestInfrastructureTest`에서 H2, 프로파일 격리, RestAssured 호출, 401, 403, 완료 사용자 JWT, 관리자 JWT 발급을 확인한다.

## 관련 문서

- 제외 범위와 이유: [exclusions.md](./exclusions.md)
- 전체 REST 엔드포인트 테스트 매트릭스: [coverage-matrix.md](./coverage-matrix.md)
- 테스트별 추가 이유: [test-rationale.md](./test-rationale.md)
