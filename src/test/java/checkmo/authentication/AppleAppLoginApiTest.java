package checkmo.authentication;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import checkmo.authentication.internal.security.apple.AppleIdTokenVerifier;
import checkmo.authentication.internal.security.apple.AppleIdentity;
import checkmo.authentication.internal.security.apple.InvalidAppleIdentityTokenException;
import checkmo.support.ApiTestSupport;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class AppleAppLoginApiTest extends ApiTestSupport {

    @MockitoBean
    AppleIdTokenVerifier appleIdTokenVerifier;

    @Test
    void appleAppLoginSucceedsWithRefreshTokenInResponseAndCookies() {
        when(appleIdTokenVerifier.verifyIosToken("valid-identity-token", "raw-nonce"))
                .thenReturn(identity("apple-sub", "apple-user@example.com"));

        ExtractableResponse<Response> response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "identityToken", "valid-identity-token",
                        "rawNonce", "raw-nonce"
                ))
                .when()
                .post("/api/v1/auth/app/apple/login")
                .then()
                .statusCode(200)
                .extract();

        String refreshToken = response.jsonPath().getString("result.refreshToken");
        assertSoftly(softly -> {
            softly.assertThat(refreshToken).isNotBlank();
            softly.assertThat(response.cookie("refreshToken")).isEqualTo(refreshToken);
            softly.assertThat(response.cookie("accessToken")).isNotBlank();
            softly.assertThat(authRepository.findByProviderAndProviderUserId("APPLE", "apple-sub")).isPresent();
        });
    }

    @Test
    void appleAppLoginReusesExistingAppleUserWithoutEmailClaim() {
        when(appleIdTokenVerifier.verifyIosToken("first-token", "raw-nonce"))
                .thenReturn(identity("repeat-sub", "repeat-apple-user@example.com"));
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identityToken", "first-token", "rawNonce", "raw-nonce"))
                .when()
                .post("/api/v1/auth/app/apple/login")
                .then()
                .statusCode(200);
        when(appleIdTokenVerifier.verifyIosToken("repeat-token", "raw-nonce"))
                .thenReturn(identityWithoutEmail("repeat-sub"));

        ExtractableResponse<Response> response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identityToken", "repeat-token", "rawNonce", "raw-nonce"))
                .when()
                .post("/api/v1/auth/app/apple/login")
                .then()
                .statusCode(200)
                .extract();

        assertSoftly(softly -> {
            softly.assertThat(response.jsonPath().getString("result.refreshToken")).isNotBlank();
            softly.assertThat(authRepository.findByProviderAndProviderUserId("APPLE", "repeat-sub")).isPresent();
            softly.assertThat(authRepository.findByEmail("repeat-apple-user@example.com")).isPresent();
        });
    }

    @Test
    void appleAppLoginRejectsEmailCollision() {
        TestUser existingUser = createUser();
        when(appleIdTokenVerifier.verifyIosToken("valid-identity-token", "raw-nonce"))
                .thenReturn(identity("new-apple-sub", existingUser.email()));

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identityToken", "valid-identity-token", "rawNonce", "raw-nonce"))
                .when()
                .post("/api/v1/auth/app/apple/login")
                .then()
                .statusCode(409)
                .body("isSuccess", equalTo(false))
                .body("code", equalTo("AUTH_414"));

        assertThat(authRepository.findByProviderAndProviderUserId("APPLE", "new-apple-sub")).isEmpty();
    }

    @Test
    void appleAppLoginRejectsInvalidIdentityToken() {
        when(appleIdTokenVerifier.verifyIosToken("invalid-identity-token", "raw-nonce"))
                .thenThrow(new InvalidAppleIdentityTokenException());

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identityToken", "invalid-identity-token", "rawNonce", "raw-nonce"))
                .when()
                .post("/api/v1/auth/app/apple/login")
                .then()
                .statusCode(401)
                .body("isSuccess", equalTo(false))
                .body("code", equalTo("AUTH_415"));
    }

    @Test
    void appleAppLoginRejectsMissingRequiredPayload() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("identityToken", "valid-identity-token"))
                .when()
                .post("/api/v1/auth/app/apple/login")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    private AppleIdentity identity(String subject, String email) {
        return new AppleIdentity(
                subject,
                "APPLE_" + subject,
                email,
                true,
                false,
                "kr.co.checkmo.app"
        );
    }

    private AppleIdentity identityWithoutEmail(String subject) {
        return new AppleIdentity(
                subject,
                "APPLE_" + subject,
                null,
                false,
                false,
                "kr.co.checkmo.app"
        );
    }
}
