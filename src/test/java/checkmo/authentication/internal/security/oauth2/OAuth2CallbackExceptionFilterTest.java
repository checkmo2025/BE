package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import checkmo.authentication.internal.security.jwt.JwtCookieUtil;
import checkmo.common.monitoring.SentryCaptureClient;
import jakarta.servlet.ServletException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

class OAuth2CallbackExceptionFilterTest {

    private final OAuth2AuthenticationFailureHandler failureHandler =
            mock(OAuth2AuthenticationFailureHandler.class);
    private final SentryCaptureClient sentryCaptureClient = mock(SentryCaptureClient.class);
    private final OAuth2CallbackExceptionFilter filter = new OAuth2CallbackExceptionFilter(
            failureHandler,
            sentryCaptureClient,
            new JwtCookieUtil()
    );

    @Test
    void skipsNonOAuthCallbackRequests() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/books");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean continued = new AtomicBoolean();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> continued.set(true));

        assertThat(continued).isTrue();
        verifyNoInteractions(failureHandler, sentryCaptureClient);
    }

    @Test
    void capturesSanitizedDiagnosticAndDelegatesControlledFailure() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/login/oauth2/code/google");
        MockHttpServletResponse response = new MockHttpServletResponse();
        IllegalStateException rootCause = new IllegalStateException("code=secret-root-value");
        ServletException failure = new ServletException("token=secret-outer-value", rootCause);
        ListAppender<ILoggingEvent> appender = attachLogAppender();

        try {
            filter.doFilter(request, response, (servletRequest, servletResponse) -> {
                throw failure;
            });

            ArgumentCaptor<Throwable> sentryCaptor = ArgumentCaptor.forClass(Throwable.class);
            verify(sentryCaptureClient).captureException(sentryCaptor.capture());
            ArgumentCaptor<AuthenticationException> delegatedCaptor =
                    ArgumentCaptor.forClass(AuthenticationException.class);
            verify(failureHandler).onAuthenticationFailure(
                    same(request),
                    same(response),
                    delegatedCaptor.capture()
            );

            Throwable diagnostic = sentryCaptor.getValue();
            AuthenticationException delegated = delegatedCaptor.getValue();
            assertSoftly(softly -> {
                softly.assertThat(diagnostic).isNotSameAs(failure);
                softly.assertThat(diagnostic.getMessage())
                        .contains("provider=google")
                        .contains("type=ServletException")
                        .contains("rootType=IllegalStateException")
                        .doesNotContain("secret-root-value")
                        .doesNotContain("secret-outer-value");
                softly.assertThat(diagnostic.getStackTrace()).isEqualTo(rootCause.getStackTrace());
                softly.assertThat(delegated).isInstanceOf(OAuth2AuthenticationException.class);
                softly.assertThat(((OAuth2AuthenticationException) delegated).getError().getErrorCode())
                        .isEqualTo("oauth2_processing_failed");
                softly.assertThat(delegated.getCause()).isSameAs(diagnostic);
                softly.assertThat(response.getHeaders("Set-Cookie"))
                        .anyMatch(header -> header.startsWith("accessToken=") && header.contains("Max-Age=0"))
                        .anyMatch(header -> header.startsWith("refreshToken=") && header.contains("Max-Age=0"));
                softly.assertThat(appender.list)
                        .filteredOn(event -> event.getLevel().equals(Level.ERROR))
                        .singleElement()
                        .satisfies(event -> {
                            softly.assertThat(event.getFormattedMessage()).contains("provider=google");
                            softly.assertThat(event.getFormattedMessage()).contains("rootType=IllegalStateException");
                            softly.assertThat(event.getFormattedMessage()).doesNotContain("secret-root-value");
                            softly.assertThat(event.getFormattedMessage()).doesNotContain("secret-outer-value");
                            softly.assertThat(event.getThrowableProxy()).isNull();
                        });
            });
        } finally {
            detachLogAppender(appender);
        }
    }

    private ListAppender<ILoggingEvent> attachLogAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(OAuth2CallbackExceptionFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private void detachLogAppender(ListAppender<ILoggingEvent> appender) {
        Logger logger = (Logger) LoggerFactory.getLogger(OAuth2CallbackExceptionFilter.class);
        logger.detachAppender(appender);
        appender.stop();
    }
}
