package checkmo.support;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

class ApiTestInfrastructureTest extends ApiTestSupport {

    @Autowired
    DataSource dataSource;

    @Autowired
    Environment environment;

    @Test
    void testProfileUsesH2AndDoesNotIncludeProductionProfiles() throws Exception {
        assertThat(environment.getActiveProfiles()).contains("test");
        assertThat(environment.getActiveProfiles())
                .doesNotContain("prod", "redis", "aladin", "mail", "jwt", "oauth2", "s3");
        assertThat(environment.getProperty("spring.flyway.enabled")).isEqualTo("false");

        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).containsIgnoringCase("H2");
        }
    }

    @Test
    void healthEndpointIsCallableWithRestAssured() {
        given()
                .when()
                .get("/health")
                .then()
                .statusCode(200);
    }

    @Test
    void authenticatedFixtureCanCallProfileCompletedApi() {
        TestUser user = createUser();

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/members/me/follow-count")
                .then()
                .statusCode(200);
    }

    @Test
    void anonymousRequestToAuthenticatedApiReturnsUnauthorized() {
        given()
                .when()
                .get("/api/v1/members/me/follow-count")
                .then()
                .statusCode(401);
    }

    @Test
    void incompleteProfileFixtureIsBlockedBeforeProtectedApiExecution() {
        TestUser user = createIncompleteUser();

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/members/me/follow-count")
                .then()
                .statusCode(403);
    }

    @Test
    void adminFixtureCanIssueJwtCookies() {
        TestUser admin = createAdmin();

        assertThat(admin.role()).isEqualTo(checkmo.authentication.internal.entity.Role.ADMIN);
        assertThat(accessTokenCookie(admin).getValue()).isNotBlank();
        assertThat(refreshTokenCookie(admin).getValue()).isNotBlank();
    }
}
