package checkmo.authentication;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.support.ApiTestSupport;
import io.restassured.http.Cookie;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthApiTest extends ApiTestSupport {

    @Test
    void sendEmailVerificationSucceedsWithMailBoundaryMocked() {
        given()
                .queryParam("email", "new-user@example.com")
                .when()
                .post("/api/v1/auth/email-verification")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("result", equalTo("인증번호가 이메일로 발송되었습니다."));
    }

    @Test
    void sendEmailVerificationRejectsInvalidEmail() {
        given()
                .queryParam("email", "not-an-email")
                .when()
                .post("/api/v1/auth/email-verification")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void confirmEmailVerificationSucceedsWhenCodeMatches() {
        String email = "verified@example.com";
        when(redisHashOperations.get("verification:" + email, "code")).thenReturn("123456");
        when(redisHashOperations.get("verification:" + email, "verified")).thenReturn(false);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("email", email, "verificationCode", "123456"))
                .when()
                .post("/api/v1/auth/email-verification/confirm")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));
    }

    @Test
    void confirmEmailVerificationRejectsWrongCode() {
        String email = "wrong-code@example.com";
        when(redisHashOperations.get("verification:" + email, "code")).thenReturn("123456");
        when(redisHashOperations.get("verification:" + email, "verified")).thenReturn(false);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("email", email, "verificationCode", "999999"))
                .when()
                .post("/api/v1/auth/email-verification/confirm")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void confirmEmailVerificationRejectsExpiredCode() {
        String email = "expired-code@example.com";
        when(redisHashOperations.get("verification:" + email, "code")).thenReturn(null);
        when(redisHashOperations.get("verification:" + email, "verified")).thenReturn(null);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("email", email, "verificationCode", "123456"))
                .when()
                .post("/api/v1/auth/email-verification/confirm")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void signUpSucceedsAfterEmailVerificationAndSetsJwtCookies() {
        String email = "signup-success@example.com";
        when(redisHashOperations.get("verification:" + email, "verified")).thenReturn(true);

        ExtractableResponse<Response> response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("email", email, "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/signup")
                .then()
                .statusCode(200)
                .body("result.email", equalTo(email))
                .body("result.profileCompleted", equalTo(false))
                .extract();

        assertJwtCookiesWereSet(response);
    }

    @Test
    void signUpAllowsMultipleIncompleteUsersWithoutNicknameConflicts() {
        String firstEmail = "signup-null-nickname-1@example.com";
        String secondEmail = "signup-null-nickname-2@example.com";
        when(redisHashOperations.get("verification:" + firstEmail, "verified")).thenReturn(true);
        when(redisHashOperations.get("verification:" + secondEmail, "verified")).thenReturn(true);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("email", firstEmail, "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/signup")
                .then()
                .statusCode(200);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("email", secondEmail, "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/signup")
                .then()
                .statusCode(200);

        var firstUser = authRepository.findByEmail(firstEmail).orElseThrow();
        var secondUser = authRepository.findByEmail(secondEmail).orElseThrow();
        var firstMember = memberRepository.findById(firstUser.getId()).orElseThrow();
        var secondMember = memberRepository.findById(secondUser.getId()).orElseThrow();

        assertThat(firstUser.getNickName()).isNull();
        assertThat(secondUser.getNickName()).isNull();
        assertThat(firstMember.getNickName()).isNull();
        assertThat(secondMember.getNickName()).isNull();
        assertThat(firstUser.isProfileCompleted()).isFalse();
        assertThat(secondUser.isProfileCompleted()).isFalse();
    }

    @Test
    void signUpRejectsUnverifiedEmail() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("email", "not-verified@example.com", "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/signup")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void signUpRejectsDuplicateEmail() {
        TestUser existing = createUser();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("email", existing.email(), "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/signup")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void signUpRejectsInvalidPayload() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("email", "bad-email", "password", "short"))
                .when()
                .post("/api/v1/auth/signup")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void loginSucceedsWithCookieOnlyWebResponse() {
        TestUser user = createUserWithPassword("Pass123!");

        ExtractableResponse<Response> response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identifier", user.email(), "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract();

        assertJwtCookiesWereSet(response);
        assertThat(response.jsonPath().getString("result")).isEqualTo("로그인에 성공했습니다.");
        assertThat(response.jsonPath().getString("result.refreshToken")).isNull();
    }

    @Test
    void 닉네임_로그인은_대소문자를_구분하지_않는다() {
        TestUser user = createUserWithPassword("Pass123!");
        var authUser = authRepository.findById(user.memberId()).orElseThrow();
        authUser.updateNickname("BookMo");
        authRepository.saveAndFlush(authUser);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identifier", "bookmo", "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    void 한글_닉네임_로그인은_NFC와_대소문자를_무시한다() {
        TestUser user = createUserWithPassword("Pass123!");
        var authUser = authRepository.findById(user.memberId()).orElseThrow();
        authUser.updateNickname("책Mo");
        authRepository.saveAndFlush(authUser);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identifier", "책mo", "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    void appLoginSucceedsWithRefreshTokenInResponse() {
        TestUser user = createUserWithPassword("Pass123!");

        ExtractableResponse<Response> response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identifier", user.email(), "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/app/login")
                .then()
                .statusCode(200)
                .extract();

        assertAppTokenCookieWasSet(response);
        String responseRefreshToken = response.jsonPath().getString("result.refreshToken");
        assertSoftly(softly -> {
            softly.assertThat(responseRefreshToken).isNotBlank();
            softly.assertThat(response.headers().getValues("Set-Cookie"))
                    .anyMatch(header -> header.startsWith("refreshToken=") && header.contains("Max-Age=0"));
        });
    }

    @Test
    void appOAuthExchangeSucceedsWithRefreshTokenInResponse() {
        when(tokenCacheService.consumeOAuthExchangeCode("oauth-code"))
                .thenReturn("true|refresh-token");

        ExtractableResponse<Response> response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("code", "oauth-code"))
                .when()
                .post("/api/v1/auth/app/oauth/exchange")
                .then()
                .statusCode(200)
                .extract();

        assertSoftly(softly -> {
            softly.assertThat(response.jsonPath().getString("result.refreshToken"))
                    .isEqualTo("refresh-token");
            softly.assertThat(response.jsonPath().getBoolean("result.profileCompleted"))
                    .isTrue();
            softly.assertThat(response.headers().getValues("Set-Cookie"))
                    .noneMatch(header -> header.startsWith("accessToken="))
                    .noneMatch(header -> header.startsWith("refreshToken="));
        });
    }

    @Test
    void appOAuthExchangeRejectsReusedCode() {
        when(tokenCacheService.consumeOAuthExchangeCode("oauth-code"))
                .thenReturn("false|refresh-token")
                .thenReturn(null);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("code", "oauth-code"))
                .when()
                .post("/api/v1/auth/app/oauth/exchange")
                .then()
                .statusCode(200);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("code", "oauth-code"))
                .when()
                .post("/api/v1/auth/app/oauth/exchange")
                .then()
                .statusCode(401)
                .body("isSuccess", equalTo(false))
                .body("code", equalTo("AUTH_416"))
                .body("message", equalTo("유효하지 않거나 만료된 인증 코드입니다. 다시 로그인해주세요."));
    }

    @Test
    void appOAuthExchangeRejectsBlankCode() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("code", ""))
                .when()
                .post("/api/v1/auth/app/oauth/exchange")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void appOAuthExchangeRejectsMalformedStoredValue() {
        when(tokenCacheService.consumeOAuthExchangeCode("oauth-code"))
                .thenReturn("malformed-value");

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("code", "oauth-code"))
                .when()
                .post("/api/v1/auth/app/oauth/exchange")
                .then()
                .statusCode(401)
                .body("isSuccess", equalTo(false))
                .body("code", equalTo("AUTH_416"));
    }

    @Test
    void appRefreshSucceedsWithHeaderOnlyRefreshTokenAndRejectsReplay() {
        TestUser user = createUserWithPassword("Pass123!");
        ExtractableResponse<Response> loginResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identifier", user.email(), "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/app/login")
                .then()
                .statusCode(200)
                .extract();
        String oldRefreshToken = loginResponse.jsonPath().getString("result.refreshToken");

        ExtractableResponse<Response> refreshResponse = given()
                .header("X-Refresh-Token", oldRefreshToken)
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .statusCode(200)
                .extract();

        assertAppTokenCookieWasSet(refreshResponse);
        String newRefreshToken = refreshResponse.jsonPath().getString("result.refreshToken");
        assertThat(newRefreshToken).isNotBlank();
        assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);

        given()
                .header("X-Refresh-Token", oldRefreshToken)
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .statusCode(401)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void sameMemberAppSessionsRotateIndependently() {
        TestUser user = createUserWithPassword("Pass123!");
        String firstRefreshToken = appLoginRefreshToken(user);
        String secondRefreshToken = appLoginRefreshToken(user);

        String rotatedFirstToken = refreshAppToken(firstRefreshToken);
        String rotatedSecondToken = refreshAppToken(secondRefreshToken);

        assertSoftly(softly -> {
            softly.assertThat(firstRefreshToken).isNotEqualTo(secondRefreshToken);
            softly.assertThat(rotatedFirstToken).isNotEqualTo(firstRefreshToken);
            softly.assertThat(rotatedSecondToken).isNotEqualTo(secondRefreshToken);
            softly.assertThat(appRefreshStatus(firstRefreshToken)).isEqualTo(401);
            softly.assertThat(appRefreshStatus(secondRefreshToken)).isEqualTo(401);
            softly.assertThat(appRefreshStatus(rotatedFirstToken)).isEqualTo(200);
            softly.assertThat(appRefreshStatus(rotatedSecondToken)).isEqualTo(200);
        });
    }

    @Test
    void appAndWebSessionsDoNotInvalidateEachOther() {
        TestUser user = createUserWithPassword("Pass123!");
        ExtractableResponse<Response> webLoginResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identifier", user.email(), "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract();
        String webRefreshToken = webLoginResponse.cookie("refreshToken");
        String appRefreshToken = appLoginRefreshToken(user);

        assertSoftly(softly -> {
            softly.assertThat(appRefreshStatus(webRefreshToken)).isEqualTo(200);
            softly.assertThat(appRefreshStatus(appRefreshToken)).isEqualTo(200);
        });
    }

    @Test
    void appLogoutRevokesOnlyTargetSession() {
        TestUser user = createUserWithPassword("Pass123!");
        String firstRefreshToken = appLoginRefreshToken(user);
        String secondRefreshToken = appLoginRefreshToken(user);

        given()
                .header("X-Refresh-Token", firstRefreshToken)
                .when()
                .post("/api/v1/auth/app/logout")
                .then()
                .statusCode(200);

        assertSoftly(softly -> {
            softly.assertThat(appRefreshStatus(firstRefreshToken)).isEqualTo(401);
            softly.assertThat(appRefreshStatus(secondRefreshToken)).isEqualTo(200);
        });
    }

    @Test
    void webLogoutRevokesOnlyWebSession() {
        TestUser user = createUserWithPassword("Pass123!");
        ExtractableResponse<Response> webLoginResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identifier", user.email(), "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract();
        String webAccessToken = webLoginResponse.cookie("accessToken");
        String webRefreshToken = webLoginResponse.cookie("refreshToken");
        String appRefreshToken = appLoginRefreshToken(user);

        given()
                .cookie("accessToken", webAccessToken)
                .cookie("refreshToken", webRefreshToken)
                .when()
                .post("/api/v1/auth/logout")
                .then()
                .statusCode(200);

        assertSoftly(softly -> {
            softly.assertThat(appRefreshStatus(webRefreshToken)).isEqualTo(401);
            softly.assertThat(appRefreshStatus(appRefreshToken)).isEqualTo(200);
        });
    }

    @Test
    void appRefreshAllowsOnlyOneConcurrentReplayWithSameRefreshToken() throws Exception {
        TestUser user = createUserWithPassword("Pass123!");
        ExtractableResponse<Response> loginResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identifier", user.email(), "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/app/login")
                .then()
                .statusCode(200)
                .extract();
        String refreshToken = loginResponse.jsonPath().getString("result.refreshToken");
        CountDownLatch bothRequestsEnteredRotation = new CountDownLatch(2);

        doAnswer(invocation -> {
            bothRequestsEnteredRotation.countDown();
            if (!bothRequestsEnteredRotation.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("concurrent refresh requests did not reach token rotation together");
            }

            Long userId = invocation.getArgument(0);
            String sessionId = invocation.getArgument(1);
            String expectedRefreshToken = invocation.getArgument(2);
            String newRefreshToken = invocation.getArgument(3);
            return rotateRefreshTokenIfCurrent(userId, sessionId, expectedRefreshToken, newRefreshToken);
        }).when(tokenCacheService).compareAndRotateRefreshToken(
                anyLong(),
                anyString(),
                anyString(),
                anyString(),
                org.mockito.ArgumentMatchers.any(Duration.class)
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<Integer>> responses = List.of(
                    executor.submit(() -> appRefreshStatus(refreshToken)),
                    executor.submit(() -> appRefreshStatus(refreshToken))
            );

            List<Integer> statuses = responses.stream()
                    .map(this::getFutureStatus)
                    .toList();

            assertThat(statuses).containsExactlyInAnyOrder(200, 401);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void differentSessionsCanRefreshConcurrently() {
        TestUser user = createUserWithPassword("Pass123!");
        String firstRefreshToken = appLoginRefreshToken(user);
        String secondRefreshToken = appLoginRefreshToken(user);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<Integer>> responses = List.of(
                    executor.submit(() -> appRefreshStatus(firstRefreshToken)),
                    executor.submit(() -> appRefreshStatus(secondRefreshToken))
            );

            assertThat(responses.stream().map(this::getFutureStatus).toList())
                    .containsExactlyInAnyOrder(200, 200);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void appRefreshRejectsMissingRefreshToken() {
        given()
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .statusCode(401)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void appRefreshRejectsInvalidRefreshToken() {
        given()
                .header("X-Refresh-Token", "invalid-refresh-token")
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .statusCode(401)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void appRefreshRejectsExpiredSignedRefreshTokenWithRefreshTokenError() {
        TestUser user = createUserWithPassword("Pass123!");
        String expiredRefreshToken = expiredSignedRefreshToken(user);
        saveRefreshTokenInCacheFake(user.id(), expiredRefreshToken);

        given()
                .header("X-Refresh-Token", expiredRefreshToken)
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .statusCode(401)
                .body("isSuccess", equalTo(false))
                .body("code", equalTo("AUTH_412"))
                .body("message", equalTo("유효하지 않은 리프레시 토큰입니다. 다시 로그인해주세요."));
    }

    @Test
    void protectedRouteDoesNotRotateTokensFromHeaderOnlyRefreshToken() {
        TestUser user = createUserWithPassword("Pass123!");
        String refreshToken = appLoginRefreshToken(user);

        ExtractableResponse<Response> protectedResponse = given()
                .header("X-Refresh-Token", refreshToken)
                .when()
                .get("/api/v1/members/me")
                .then()
                .extract();

        assertThat(protectedResponse.statusCode()).isIn(401, 403);
        assertThat(protectedResponse.headers().getValues("Set-Cookie"))
                .noneMatch(header -> header.startsWith("accessToken="))
                .noneMatch(header -> header.startsWith("refreshToken="));

        given()
                .header("X-Refresh-Token", refreshToken)
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .statusCode(200);
    }

    @Test
    void protectedRouteRejectsLegacyStringSubjectAccessTokenWithoutServerError() {
        TestUser user = createUserWithPassword("Pass123!");
        String legacyAccessToken = signedAccessTokenWithSubject(user.legacyId());

        ExtractableResponse<Response> response = given()
                .cookie(new Cookie.Builder("accessToken", legacyAccessToken)
                        .setPath("/")
                        .build())
                .when()
                .get("/api/v1/members/me")
                .then()
                .extract();

        assertThat(response.statusCode()).isIn(401, 403);
        assertThat(response.headers().getValues("Set-Cookie"))
                .anyMatch(header -> header.startsWith("accessToken=") && header.contains("Max-Age=0"))
                .anyMatch(header -> header.startsWith("refreshToken=") && header.contains("Max-Age=0"));
    }

    @Test
    void appRefreshRejectsLegacyStringSubjectRefreshTokenWithoutServerError() {
        TestUser user = createUserWithPassword("Pass123!");
        String legacyRefreshToken = signedRefreshTokenWithSubject(user.legacyId());
        saveRefreshTokenInCacheFake(user.legacyId(), legacyRefreshToken);

        given()
                .header("X-Refresh-Token", legacyRefreshToken)
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .statusCode(401)
                .body("isSuccess", equalTo(false))
                .body("code", equalTo("AUTH_412"));
    }

    @Test
    void loginRejectsWrongPassword() {
        TestUser user = createUserWithPassword("Pass123!");

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identifier", user.email(), "password", "Wrong1!"))
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(401)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void logoutClearsJwtCookiesWhenTokensExist() {
        TestUser user = createUser();

        ExtractableResponse<Response> response = given()
                .cookie(accessTokenCookie(user))
                .cookie(refreshTokenCookie(user))
                .when()
                .post("/api/v1/auth/logout")
                .then()
                .statusCode(200)
                .extract();

        assertThat(response.headers().getValues("Set-Cookie"))
                .anyMatch(header -> header.startsWith("accessToken=") && header.contains("Max-Age=0"))
                .anyMatch(header -> header.startsWith("refreshToken=") && header.contains("Max-Age=0"));
    }

    @Test
    void logoutWithStaleRotatedRefreshCookieDoesNotInvalidateCurrentSession() {
        TestUser user = createUserWithPassword("Pass123!");
        String staleRefreshToken = appLoginRefreshToken(user);
        String currentRefreshToken = given()
                .header("X-Refresh-Token", staleRefreshToken)
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("result.refreshToken");

        ExtractableResponse<Response> response = given()
                .cookie(new Cookie.Builder("refreshToken", staleRefreshToken)
                        .setPath("/")
                        .build())
                .when()
                .post("/api/v1/auth/logout")
                .then()
                .statusCode(200)
                .extract();

        assertThat(response.headers().getValues("Set-Cookie"))
                .anyMatch(header -> header.startsWith("accessToken=") && header.contains("Max-Age=0"))
                .anyMatch(header -> header.startsWith("refreshToken=") && header.contains("Max-Age=0"));

        given()
                .header("X-Refresh-Token", currentRefreshToken)
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .statusCode(200);
    }

    @Test
    void appLogoutInvalidatesHeaderOnlyRefreshToken() {
        TestUser user = createUserWithPassword("Pass123!");
        ExtractableResponse<Response> loginResponse = appLogin(user);
        String refreshToken = loginResponse.jsonPath().getString("result.refreshToken");

        given()
                .header("X-Refresh-Token", refreshToken)
                .when()
                .post("/api/v1/auth/app/logout")
                .then()
                .statusCode(200);

        given()
                .header("X-Refresh-Token", refreshToken)
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .statusCode(401)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void appLogoutBlacklistsAccessTokenFromMatchingSession() {
        TestUser user = createUserWithPassword("Pass123!");
        ExtractableResponse<Response> loginResponse = appLogin(user);
        String accessToken = loginResponse.cookie("accessToken");
        String refreshToken = loginResponse.jsonPath().getString("result.refreshToken");

        given()
                .header("X-Refresh-Token", refreshToken)
                .cookie("accessToken", accessToken)
                .when()
                .post("/api/v1/auth/app/logout")
                .then()
                .statusCode(200);

        verify(tokenCacheService).saveBlacklistToken(accessToken);
    }

    @Test
    void appLogoutDoesNotBlacklistAccessTokenFromAnotherSessionOfSameMember() {
        TestUser user = createUserWithPassword("Pass123!");
        ExtractableResponse<Response> firstLoginResponse = appLogin(user);
        ExtractableResponse<Response> secondLoginResponse = appLogin(user);
        String firstRefreshToken = firstLoginResponse.jsonPath().getString("result.refreshToken");
        String secondAccessToken = secondLoginResponse.cookie("accessToken");
        String secondRefreshToken = secondLoginResponse.jsonPath().getString("result.refreshToken");

        given()
                .header("X-Refresh-Token", firstRefreshToken)
                .cookie("accessToken", secondAccessToken)
                .when()
                .post("/api/v1/auth/app/logout")
                .then()
                .statusCode(200);

        verify(tokenCacheService, never()).saveBlacklistToken(secondAccessToken);
        assertSoftly(softly -> {
            softly.assertThat(appRefreshStatus(firstRefreshToken)).isEqualTo(401);
            softly.assertThat(appRefreshStatus(secondRefreshToken)).isEqualTo(200);
        });
    }

    @Test
    void appLogoutKeepsSidlessLegacyTokenCompatibility() {
        TestUser user = createUserWithPassword("Pass123!");
        String legacyAccessToken = signedAccessTokenWithSubject(user.id());
        String legacyRefreshToken = signedRefreshTokenWithSubject(user.id());
        saveRefreshTokenInCacheFake(user.id(), legacyRefreshToken);

        given()
                .header("X-Refresh-Token", legacyRefreshToken)
                .cookie("accessToken", legacyAccessToken)
                .when()
                .post("/api/v1/auth/app/logout")
                .then()
                .statusCode(200);

        verify(tokenCacheService).saveBlacklistToken(legacyAccessToken);
        assertThat(appRefreshStatus(legacyRefreshToken)).isEqualTo(401);
    }

    @Test
    void appLogoutPrefersHeaderRefreshTokenOverDifferentCookieRefreshToken() {
        TestUser headerUser = createUserWithPassword("Pass123!");
        TestUser cookieUser = createUserWithPassword("Pass123!");
        String headerRefreshToken = appLoginRefreshToken(headerUser);
        String cookieRefreshToken = appLoginRefreshToken(cookieUser);

        given()
                .header("X-Refresh-Token", headerRefreshToken)
                .cookie(new Cookie.Builder("refreshToken", cookieRefreshToken)
                        .setPath("/")
                        .build())
                .when()
                .post("/api/v1/auth/app/logout")
                .then()
                .statusCode(200);

        given()
                .header("X-Refresh-Token", headerRefreshToken)
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .statusCode(401)
                .body("isSuccess", equalTo(false));

        given()
                .header("X-Refresh-Token", cookieRefreshToken)
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .statusCode(200);
    }

    @Test
    void appLogoutRejectsStaleRotatedRefreshTokenWithoutInvalidatingCurrentSession() {
        TestUser user = createUserWithPassword("Pass123!");
        String staleRefreshToken = appLoginRefreshToken(user);
        String currentRefreshToken = given()
                .header("X-Refresh-Token", staleRefreshToken)
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("result.refreshToken");

        ExtractableResponse<Response> response = given()
                .header("X-Refresh-Token", staleRefreshToken)
                .when()
                .post("/api/v1/auth/app/logout")
                .then()
                .statusCode(401)
                .body("isSuccess", equalTo(false))
                .extract();

        assertThat(response.headers().getValues("Set-Cookie"))
                .noneMatch(header -> header.startsWith("accessToken=") && header.contains("Max-Age=0"))
                .noneMatch(header -> header.startsWith("refreshToken=") && header.contains("Max-Age=0"));
        verify(tokenCacheService, never()).saveBlacklistToken(anyString());

        given()
                .header("X-Refresh-Token", currentRefreshToken)
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .statusCode(200);
    }

    @Test
    void sendTempPasswordSucceedsForKnownEmail() {
        TestUser user = createUser();

        given()
                .queryParam("email", user.email())
                .when()
                .post("/api/v1/auth/temp-password")
                .then()
                .statusCode(200)
                .body("result", equalTo("임시 비밀번호가 이메일로 발송되었습니다."));
    }

    @Test
    void sendTempPasswordReturnsNotFoundForUnknownEmail() {
        given()
                .queryParam("email", "missing@example.com")
                .when()
                .post("/api/v1/auth/temp-password")
                .then()
                .statusCode(404)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void sendTempPasswordRejectsInvalidEmail() {
        given()
                .queryParam("email", "missing")
                .when()
                .post("/api/v1/auth/temp-password")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    private void assertJwtCookiesWereSet(ExtractableResponse<Response> response) {
        assertThat(response.headers().getValues("Set-Cookie"))
                .anyMatch(header -> header.startsWith("accessToken=") && header.contains("HttpOnly"))
                .anyMatch(header -> header.startsWith("refreshToken=") && header.contains("HttpOnly"));
    }

    private void assertAppTokenCookieWasSet(ExtractableResponse<Response> response) {
        assertThat(response.headers().getValues("Set-Cookie"))
                .anyMatch(header -> header.startsWith("accessToken=") && header.contains("HttpOnly"))
                .anyMatch(header -> header.startsWith("refreshToken=") && header.contains("Max-Age=0"));
    }

    private String appLoginRefreshToken(TestUser user) {
        return appLogin(user)
                .jsonPath()
                .getString("result.refreshToken");
    }

    private ExtractableResponse<Response> appLogin(TestUser user) {
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identifier", user.email(), "password", "Pass123!"))
                .when()
                .post("/api/v1/auth/app/login")
                .then()
                .statusCode(200)
                .extract();
    }

    private int appRefreshStatus(String refreshToken) {
        return given()
                .header("X-Refresh-Token", refreshToken)
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .extract()
                .statusCode();
    }

    private String refreshAppToken(String refreshToken) {
        return given()
                .header("X-Refresh-Token", refreshToken)
                .when()
                .post("/api/v1/auth/app/refresh")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("result.refreshToken");
    }

    private int getFutureStatus(Future<Integer> future) {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new AssertionError("concurrent refresh request did not complete", e);
        }
    }
}
