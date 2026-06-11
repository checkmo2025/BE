package checkmo.common.apiPayload.exception;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import checkmo.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "sentry-qa"})
class SentryExceptionAdviceApiTest extends ApiTestSupport {

    @Test
    void returnsExistingApiErrorShapeWhenUnhandledExceptionIsCaptured() {
        String before = given()
                .when()
                .get("/api/test/sentry/captures/count")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        String body = given()
                .queryParam("message", "secret-refresh-token")
                .when()
                .get("/api/test/sentry/unexpected")
                .then()
                .statusCode(500)
                .extract()
                .asString();

        String after = given()
                .when()
                .get("/api/test/sentry/captures/count")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        assertThat(body)
                .contains("\"isSuccess\":false")
                .contains("\"code\":\"COMMON_500\"")
                .contains("\"message\":\"서버 에러, 관리자에게 문의 바랍니다.\"")
                .doesNotContain("secret-refresh-token");
        assertThat(Integer.parseInt(after)).isEqualTo(Integer.parseInt(before) + 1);
    }

    @Test
    void healthEndpointStillWorksWithSentryConfigured() {
        given()
                .when()
                .get("/health")
                .then()
                .statusCode(200);
    }

}
