package checkmo.member;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import checkmo.support.ApiTestSupport;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class PublicMemberApiTest extends ApiTestSupport {

    @Test
    void checkNicknameReturnsDuplicatedState() {
        TestUser user = createUser();

        given()
                .queryParam("nickname", user.nickName())
                .when()
                .post("/api/members/check-nickname")
                .then()
                .statusCode(200)
                .body("result", equalTo(true));
    }

    @Test
    void checkNicknameRejectsInvalidNickname() {
        given()
                .queryParam("nickname", "한글닉네임")
                .when()
                .post("/api/members/check-nickname")
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
                .post("/api/members/find-email")
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
                .post("/api/members/find-email")
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
                .post("/api/members/find-email")
                .then()
                .statusCode(400)
                .body("isSuccess", equalTo(false));
    }

    @Test
    void protectedMemberApiReturnsUnauthorizedWithoutCookie() {
        given()
                .when()
                .get("/api/members/me")
                .then()
                .statusCode(401);
    }
}
