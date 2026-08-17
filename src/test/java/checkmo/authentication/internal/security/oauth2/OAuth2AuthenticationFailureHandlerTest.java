package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2AuthenticationFailureHandlerTest {

    @Test
    void redirectsWithoutSecretQuery() throws Exception {
        OAuth2AuthenticationFailureHandler handler = new OAuth2AuthenticationFailureHandler();
        ReflectionTestUtils.setField(handler, "baseUri", "https://web.checkmo.test");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(
                "provider failed with token=secret-value"
        );
        ListAppender<ILoggingEvent> appender = attachLogAppender();

        try {
            handler.onAuthenticationFailure(request, response, exception);

            assertSoftly(softly -> {
                softly.assertThat(response.getRedirectedUrl())
                        .isEqualTo("https://web.checkmo.test/?error=login_failed");
                softly.assertThat(response.getRedirectedUrl()).doesNotContain("secret-value");
                softly.assertThat(response.getRedirectedUrl()).doesNotContain("token=");
                softly.assertThat(appender.list)
                        .filteredOn(event -> event.getLevel().equals(Level.ERROR))
                        .singleElement()
                        .satisfies(event -> {
                            softly.assertThat(event.getFormattedMessage()).contains("token=***");
                            softly.assertThat(event.getFormattedMessage()).doesNotContain("secret-value");
                            softly.assertThat(event.getThrowableProxy()).isNull();
                        });
            });
        } finally {
            detachLogAppender(appender);
        }
    }

    @Test
    void appAuthorizationFailureRedirectsDeepLinkErrorWithoutSecretQuery() throws Exception {
        OAuth2AuthenticationFailureHandler handler = new OAuth2AuthenticationFailureHandler();
        ReflectionTestUtils.setField(handler, "appUri", "checkmo://oauth-callback");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("state", "app-state");
        request.getSession().setAttribute(
                AppleOAuth2AuthorizationRequestResolver.sessionClientTypeKey("app-state"),
                AppleOAuth2AuthorizationRequestResolver.CLIENT_TYPE_APP
        );
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(
                "provider failed with token=secret-value"
        );

        handler.onAuthenticationFailure(request, response, exception);

        assertSoftly(softly -> {
            softly.assertThat(response.getRedirectedUrl())
                    .isEqualTo("checkmo://oauth-callback?error=login_failed");
            softly.assertThat(response.getRedirectedUrl()).doesNotContain("secret-value");
            softly.assertThat(response.getRedirectedUrl()).doesNotContain("token=");
            softly.assertThat(request.getSession().getAttribute(
                    AppleOAuth2AuthorizationRequestResolver.sessionClientTypeKey("app-state"))).isNull();
        });
    }

    private ListAppender<ILoggingEvent> attachLogAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private void detachLogAppender(ListAppender<ILoggingEvent> appender) {
        Logger logger = (Logger) LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);
        logger.detachAppender(appender);
        appender.stop();
    }
}
