package checkmo.member;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import checkmo.support.ApiTestSupport;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

class PublicMemberApiTest extends ApiTestSupport {

    @Test
    void checkNicknameReturnsDuplicatedState() {
        TestUser user = createUser();

        given()
                .queryParam("nickname", user.nickName())
                .when()
                .post("/api/v1/members/check-nickname")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));
    }

    @Test
    void checkNicknameAllowsHangulAndUppercaseNickname() {
        given()
                .queryParam("nickname", "한글NickName")
                .when()
                .post("/api/v1/members/check-nickname")
                .then()
                .statusCode(200)
                .body("result", equalTo(false));
    }

    @Test
    void checkNicknameTreatsCaseVariantsAsDuplicate() {
        TestUser user = createUser();

        given()
                .queryParam("nickname", user.nickName().toUpperCase(Locale.ROOT))
                .when()
                .post("/api/v1/members/check-nickname")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));
    }

    @Test
    void checkNicknameKeepsDeactivatedUsersNicknameReserved() {
        TestUser user = createUser();
        var member = memberRepository.findById(user.memberId()).orElseThrow();
        member.deactivate();
        memberRepository.saveAndFlush(member);

        given()
                .queryParam("nickname", user.nickName().toUpperCase(Locale.ROOT))
                .when()
                .post("/api/v1/members/check-nickname")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"책 모", "책\t모", "책\n모", "책\u00A0모", "책`모", "책~모", "책📚모"})
    void checkNicknameRejectsWhitespaceAndUnsupportedCharacters(String nickname) {
        given()
                .queryParam("nickname", nickname)
                .when()
                .post("/api/v1/members/check-nickname")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void findEmailSucceedsForMatchingNameAndPhoneNumber() {
        createUser();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("name", "테스트", "phoneNumber", "01012345678"))
                .when()
                .post("/api/v1/members/find-email")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    void findEmailReturnsNotFoundForUnknownPersonalInfo() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("name", "없음", "phoneNumber", "01000000000"))
                .when()
                .post("/api/v1/members/find-email")
                .then()
                .statusCode(404)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void findEmailRejectsInvalidPayload() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("name", "", "phoneNumber", ""))
                .when()
                .post("/api/v1/members/find-email")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void protectedMemberApiReturnsUnauthorizedWithoutCookie() {
        given()
                .when()
                .get("/api/v1/members/me")
                .then()
                .statusCode(401);
    }
}
