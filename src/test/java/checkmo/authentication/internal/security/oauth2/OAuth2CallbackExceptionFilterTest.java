package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import checkmo.authentication.internal.security.jwt.JwtCookieUtil;
import checkmo.common.monitoring.SentryCaptureClient;
import jakarta.servlet.ServletException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
    void capturesUnexpectedCallbackExceptionOnceAndDelegatesControlledFailure() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/login/oauth2/code/kakao");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletException failure = new ServletException("token=secret-value");

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw failure;
        });

        verify(sentryCaptureClient).captureException(failure);
        ArgumentCaptor<AuthenticationException> exceptionCaptor =
                ArgumentCaptor.forClass(AuthenticationException.class);
        verify(failureHandler).onAuthenticationFailure(
                org.mockito.ArgumentMatchers.same(request),
                org.mockito.ArgumentMatchers.same(response),
                exceptionCaptor.capture()
        );

        AuthenticationException delegated = exceptionCaptor.getValue();
        String responseBody = response.getContentAsString();
        assertSoftly(softly -> {
            softly.assertThat(delegated).isInstanceOf(OAuth2AuthenticationException.class);
            softly.assertThat(((OAuth2AuthenticationException) delegated).getError().getErrorCode())
                    .isEqualTo(OAuth2ErrorCodes.OAUTH2_PROCESSING_FAILED);
            softly.assertThat(delegated.getCause()).isSameAs(failure);
            softly.assertThat(response.getHeaders("Set-Cookie"))
                    .anyMatch(header -> header.startsWith("accessToken=") && header.contains("Max-Age=0"))
                    .anyMatch(header -> header.startsWith("refreshToken=") && header.contains("Max-Age=0"));
            softly.assertThat(responseBody).doesNotContain("secret-value");
        });
    }
}
