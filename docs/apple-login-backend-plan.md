# Apple Login Backend Plan

## 목적

체크모 React Native 앱에서 Apple 로그인을 지원하기 위해 백엔드에서 변경해야 할 인증/회원가입 흐름을 정리한다.

이 문서는 2026-06-13 기준 `checkmo_be/BE` 저장소의 `develop` 브랜치(`98c2cabf`)를 기준으로 작성했다.

## 현재 회원가입 흐름

### 일반 이메일 회원가입

1. `POST /api/auth/email-verification`에서 이메일 인증번호를 발송한다.
2. `POST /api/auth/email-verification/confirm`에서 Redis의 `verification:{email}` 해시에 `verified=true`를 기록한다.
3. `POST /api/auth/signup`은 `AuthUserCommandService.signUp()`에서 아래 순서로 처리한다.
   - `auth_user.email` 중복 확인
   - Redis 이메일 인증 완료 여부 확인
   - 비밀번호 BCrypt 인코딩
   - `AuthUser` 생성: `id = LOCAL_<8자리 UUID>`, `profileCompleted = false`
   - `AuthenticationEvent.CreateMember` 발행
4. `MemberEventListener`가 같은 트랜잭션에서 `MemberCommandService.createMember()`를 호출해 `member` row를 만든다.
5. `AuthFacade.signUp()`이 방금 만든 계정으로 로그인 처리하고 `accessToken`, `refreshToken` HttpOnly 쿠키를 내려준다.
6. 가입 직후 사용자는 프로필 미완성 상태이므로 `/api/members/additional-info`에서 닉네임, 이름, 전화번호, 관심 카테고리 등을 입력해야 한다.
7. 추가정보 저장 시 `Member`가 갱신되고 `AuthenticationAPI.updateNickname()`, `AuthenticationAPI.completeProfile()`을 통해 `AuthUser.nickName`, `AuthUser.profileCompleted`도 갱신된다.

### 기존 소셜 로그인

현재 Google/Kakao/Naver는 Spring Security OAuth2 redirect flow를 사용한다.

1. FE가 `/oauth2/authorization/{provider}`로 이동한다.
2. OAuth provider 인증 후 `/login/oauth2/code/{provider}`로 callback이 들어온다.
3. `CustomOAuth2UserService.loadUser()`가 provider user info를 읽고 `OAuth2Attributes`로 `email`, `providerId`를 추출한다.
4. 현재 코드는 `authRepository.findByEmail(email)`로 기존 회원을 찾는다.
5. 회원이 없으면 `AuthConverter.toOAuth2User()`로 `AuthUser`를 만든다.
   - `id = <PROVIDER>_<providerId>`
   - `password = ""`
   - `profileCompleted = false`
6. 신규 회원이면 `AuthenticationEvent.CreateMember`를 발행해 `member` row도 만든다.
7. `OAuth2AuthenticationSuccessHandler`가 JWT 쿠키를 세팅하고, 프로필 완료 여부에 따라 FE로 redirect한다.
   - 완료: `/`
   - 미완료: `/signup/terms?isSocial={true|false}`

### 프로필 미완성 접근 제한

`ProfileCompletionAuthorizationFilter`는 인증된 사용자의 `AuthUser.profileCompleted`가 `false`이면 대부분의 API를 403으로 차단한다.

예외 경로:

- `/api/auth/logout`
- `/api/members/additional-info`
- `/api/auth/redirect/oauth2`
- `/api/members/check-nickname`
- `/api/s3/image/upload-url`
- Swagger/API docs

Apple 신규 회원도 기존과 동일하게 `profileCompleted=false`로 생성한 뒤 추가정보 입력을 거치면 된다.

## Apple 로그인 도입 시 핵심 결정

### 권장 방식: RN 네이티브 Apple 토큰 검증 API

React Native 앱에서는 Apple 네이티브 로그인 결과로 받은 `identityToken`을 백엔드에 전달하고, 백엔드가 Apple 공개키로 토큰을 검증하는 전용 API를 두는 방식을 권장한다.

예상 API:

```http
POST /api/auth/apple
Content-Type: application/json

{
  "identityToken": "<apple id_token>",
  "authorizationCode": "<optional>",
  "nonce": "<optional>"
}
```

처리 결과는 기존 로그인과 동일하게 `accessToken`, `refreshToken` 쿠키를 세팅한다. 응답 body에는 RN이 화면 분기를 할 수 있도록 최소한 아래 값을 내려준다.

```json
{
  "email": "user@example.com",
  "profileCompleted": false,
  "newSocialSignUp": true,
  "provider": "APPLE"
}
```

이 방식이 현재 코드에 더 잘 맞는 이유:

- 기존 Spring OAuth2 redirect handler는 웹 페이지 redirect 중심이다.
- Apple은 일반 OAuth user-info endpoint가 아니라 OIDC `id_token` 중심으로 식별 정보를 준다.
- RN 앱에서는 redirect callback보다 네이티브 Apple credential을 API로 넘기는 흐름이 단순하다.
- 기존 JWT 쿠키 발급, `CreateMember` 이벤트, 추가정보 입력 흐름은 그대로 재사용할 수 있다.

### 대안: Spring Security OAuth2/OIDC redirect flow

웹 로그인도 필요하다면 Apple registration을 `application-oauth2.yml`에 추가하고 `/oauth2/authorization/apple` flow를 붙일 수 있다.

다만 이 경우 별도 고려가 필요하다.

- Apple `client_secret`은 고정 문자열이 아니라 Apple private key로 서명한 JWT이며 만료가 있다.
- `DefaultOAuth2UserService` 기반의 현재 `CustomOAuth2UserService`는 Apple에 그대로 맞지 않는다. `id_token` claim을 읽는 OIDC user service 또는 별도 success flow가 필요하다.
- Apple Developer에서 Services ID, return URL, domain 설정이 필요하다.

## 백엔드 변경 목록

### 1. Apple provider 상수 추가

`Provider`에 `APPLE`을 추가한다.

```java
public static final String APPLE = "apple";

public static abstract class Apple {
    public static final String EMAIL = "email";
    public static final String PROVIDER_ID = "sub";
}
```

`MemberQueryFacade.retrieveLoginStatus()`의 provider switch에도 `APPLE`을 추가해야 `/api/members/me/login-status` 응답이 `SOCIAL`로 떨어지지 않는다.

### 2. Apple ID token 검증 컴포넌트 추가

새 컴포넌트 예시:

- `AppleIdentityTokenVerifier`
- `ApplePublicKeyClient`
- `AppleLoginProperties`

검증해야 할 값:

- JWT 서명: Apple JWKS(`https://appleid.apple.com/auth/keys`)의 `kid`, `alg=RS256`
- `iss = https://appleid.apple.com`
- `aud`가 앱의 허용 audience 중 하나인지
  - iOS native: 보통 bundle id
  - web/services flow: Services ID
- `exp`, `iat`
- FE가 nonce를 사용한다면 `nonce`
- 신규 생성에 필요한 `email`

Apple OpenID 설정은 `https://appleid.apple.com/.well-known/openid-configuration`에서 확인할 수 있다.

### 3. Apple 로그인 서비스 추가

전용 서비스에서 아래 순서로 처리한다.

1. `identityToken` 검증
2. `sub` 추출
3. `memberId = APPLE_<sub>` 생성
4. `authRepository.findById(memberId)`로 기존 회원 조회
5. 기존 회원이면 JWT 발급
6. 신규 회원이면 email 확인 후 `AuthUser` 생성
7. `AuthenticationEvent.CreateMember(id, email)` 발행
8. `JwtLoginProcessor.processLogin()`으로 쿠키 발급

중요: Apple은 `email`보다 `sub`를 주 식별자로 삼아야 한다. 현재 소셜 로그인은 `findByEmail()`을 먼저 사용하지만, Apple은 private relay email이나 최초 동의 시점의 email 노출 정책 때문에 email만으로 계정을 식별하면 재로그인/계정 연결 이슈가 생길 수 있다.

### 4. 이메일 충돌 정책 명시

신규 Apple 로그인 시 같은 email의 기존 계정이 이미 있으면 자동으로 연결하지 않는 것을 권장한다.

권장 정책:

- `APPLE_<sub>`가 이미 있으면 로그인 성공
- `APPLE_<sub>`가 없고 동일 email 계정이 있으면 `AUTH_*` 에러 반환
- 계정 연결은 별도 기능으로 설계

이유:

- 현재 Google/Kakao/Naver는 같은 email이면 같은 계정으로 로그인시키지만, Apple 도입 시 이 정책을 그대로 따르면 provider 간 계정 소유 검증 없이 계정이 합쳐질 수 있다.
- Apple private relay email은 사용자의 실제 email과 다를 수 있다.

### 5. 설정 추가

`src/main/resources/application-oauth2.yml` 또는 별도 `application-apple.yml`에 secret 값이 아닌 placeholder만 추가한다.

예시:

```yaml
apple:
  auth:
    issuer: https://appleid.apple.com
    jwks-uri: https://appleid.apple.com/auth/keys
    allowed-audiences:
      - ${APPLE_IOS_BUNDLE_ID}
      - ${APPLE_SERVICE_ID:}
```

authorization code 교환 또는 revoke까지 백엔드에서 처리한다면 추가로 필요하다.

```yaml
apple:
  auth:
    team-id: ${APPLE_TEAM_ID}
    key-id: ${APPLE_KEY_ID}
    client-id: ${APPLE_SERVICE_ID}
    private-key: ${APPLE_PRIVATE_KEY}
```

private key 값은 절대 git에 커밋하지 않고 `.env`/CI secret으로만 주입한다.

### 6. Controller/DTO 추가

`AuthController`에 `POST /api/auth/apple`을 추가한다.

요청 DTO:

- `identityToken`: required
- `authorizationCode`: optional
- `nonce`: optional

응답 DTO:

- `email`
- `profileCompleted`
- `newSocialSignUp`
- `provider`

Security 설정에서 `/api/auth/**`는 이미 permitAll이라 별도 허용 추가는 필요 없을 가능성이 높다.

### 7. 테스트 추가

최소 테스트:

- 신규 Apple 로그인 성공 시 `AuthUser.id = APPLE_<sub>`로 저장된다.
- 신규 Apple 로그인 시 `Member` row가 생성된다.
- 성공 응답에서 JWT 쿠키가 세팅된다.
- 기존 Apple 회원 재로그인 시 회원이 중복 생성되지 않는다.
- 신규 Apple 로그인에서 email claim이 없으면 실패한다.
- 같은 email의 다른 provider 계정이 있으면 정책대로 실패한다.
- `/api/members/me/login-status`가 `provider=APPLE`을 반환한다.
- 프로필 미완성 Apple 회원은 보호 API 접근 시 403이고 `/api/members/additional-info`는 가능하다.

## 구현 시 주의점

- `.env` 값을 읽거나 문서에 옮기지 않는다.
- Apple private key, team id, key id, client id는 모두 placeholder와 운영 secret으로만 다룬다.
- `authentication` 모듈 내부 구현 변경은 `authentication` 안에 둔다.
- `member` 모듈과의 연결은 기존 `AuthenticationEvent.CreateMember`와 `AuthenticationAPI`를 재사용한다.
- DB migration은 현재 구조상 필요 없을 가능성이 높다. `AuthUser.id`는 문자열이고 social password는 이미 빈 문자열을 허용한다.
- 다만 `email`은 `AuthUser`, `Member` 모두 nullable이 아니므로 신규 Apple 회원 생성 시 email이 반드시 필요하다.

## 참고 자료

- Apple OpenID configuration: https://appleid.apple.com/.well-known/openid-configuration
- Apple JWKS: https://appleid.apple.com/auth/keys
- Apple Developer - Configure Sign in with Apple for the web: https://developer.apple.com/help/account/capabilities/configure-sign-in-with-apple-for-the-web/
- Apple Developer - Create a Sign in with Apple private key: https://developer.apple.com/help/account/capabilities/create-a-sign-in-with-apple-private-key/
