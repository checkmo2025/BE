package checkmo.authentication.internal.config;

import static io.restassured.RestAssured.given;

import checkmo.support.ApiTestSupport;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

@Import(OAuthErrorDispatchApiTest.ErrorDispatchTestConfiguration.class)
class OAuthErrorDispatchApiTest extends ApiTestSupport {

    private static final String ERROR_TRIGGER_PATH = "/api/v1/test/oauth-error-dispatch";

    @Test
    void keepsInternalErrorDispatchAsServerError() {
        given()
                .redirects().follow(false)
                .when()
                .get(ERROR_TRIGGER_PATH)
                .then()
                .statusCode(500);
    }

    @Test
    void directErrorEndpointRequestStillRequiresAuthentication() {
        given()
                .redirects().follow(false)
                .when()
                .get("/error")
                .then()
                .statusCode(401);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ErrorDispatchTestConfiguration {

        @Bean
        FilterRegistrationBean<Filter> errorDispatchTriggerFilter() {
            Filter filter = (request, response, chain) -> {
                throw new ServletException("expected test exception");
            };
            FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(filter);
            registration.setUrlPatterns(Set.of(ERROR_TRIGGER_PATH));
            registration.setDispatcherTypes(DispatcherType.REQUEST);
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return registration;
        }
    }
}
