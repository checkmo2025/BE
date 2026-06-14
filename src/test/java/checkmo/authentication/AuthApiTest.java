package checkmo.authentication;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import checkmo.support.ApiTestSupport;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthApiTest extends ApiTestSupport {

    @Test
    void sendEmailVerificationSucceedsWithMailBoundaryMocked() {
        given()
                .queryParam("email", "new-user@example.com")
                .when()
                .post("/api/auth/email-verification")
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
                .post("/api/auth/email-verification")
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
                .post("/api/auth/email-verification/confirm")
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
                .post("/api/auth/email-verification/confirm")
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
                .post("/api/auth/email-verification/confirm")
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
                .post("/api/auth/signup")
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
                .post("/api/auth/signup")
                .then()
                .statusCode(200);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("email", secondEmail, "password", "Pass123!"))
                .when()
                .post("/api/auth/signup")
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
                .post("/api/auth/signup")
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
                .post("/api/auth/signup")
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
                .post("/api/auth/signup")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void loginSucceedsAndSetsJwtCookies() {
        TestUser user = createUserWithPassword("Pass123!");

        ExtractableResponse<Response> response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identifier", user.email(), "password", "Pass123!"))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("result", equalTo("로그인에 성공했습니다."))
                .extract();

        assertJwtCookiesWereSet(response);
    }

    @Test
    void loginRejectsWrongPassword() {
        TestUser user = createUserWithPassword("Pass123!");

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identifier", user.email(), "password", "Wrong1!"))
                .when()
                .post("/api/auth/login")
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
                .post("/api/auth/logout")
                .then()
                .statusCode(200)
                .extract();

        assertThat(response.headers().getValues("Set-Cookie"))
                .anyMatch(header -> header.startsWith("accessToken=") && header.contains("Max-Age=0"))
                .anyMatch(header -> header.startsWith("refreshToken=") && header.contains("Max-Age=0"));
    }

    @Test
    void sendTempPasswordSucceedsForKnownEmail() {
        TestUser user = createUser();

        given()
                .queryParam("email", user.email())
                .when()
                .post("/api/auth/temp-password")
                .then()
                .statusCode(200)
                .body("result", equalTo("임시 비밀번호가 이메일로 발송되었습니다."));
    }

    @Test
    void sendTempPasswordReturnsNotFoundForUnknownEmail() {
        given()
                .queryParam("email", "missing@example.com")
                .when()
                .post("/api/auth/temp-password")
                .then()
                .statusCode(404)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void sendTempPasswordRejectsInvalidEmail() {
        given()
                .queryParam("email", "missing")
                .when()
                .post("/api/auth/temp-password")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    private void assertJwtCookiesWereSet(ExtractableResponse<Response> response) {
        assertThat(response.headers().getValues("Set-Cookie"))
                .anyMatch(header -> header.startsWith("accessToken=") && header.contains("HttpOnly"))
                .anyMatch(header -> header.startsWith("refreshToken=") && header.contains("HttpOnly"));
    }
}
