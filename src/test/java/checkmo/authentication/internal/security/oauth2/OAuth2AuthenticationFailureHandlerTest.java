package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2AuthenticationFailureHandlerTest {

    @Test
    void redirectsWithoutSecretQuery() throws Exception {
        OAuth2AuthenticationFailureHandler handler = new OAuth2AuthenticationFailureHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(
                "provider failed with token=secret-value"
        );

        handler.onAuthenticationFailure(request, response, exception);

        assertSoftly(softly -> {
            softly.assertThat(response.getRedirectedUrl()).isEqualTo("/login?error=true");
            softly.assertThat(response.getRedirectedUrl()).doesNotContain("secret-value");
            softly.assertThat(response.getRedirectedUrl()).doesNotContain("token=");
        });
    }

    @Test
    void appAuthorizationFailureRedirectsDeepLinkErrorWithoutSecretQuery() throws Exception {
        OAuth2AuthenticationFailureHandler handler = new OAuth2AuthenticationFailureHandler();
        ReflectionTestUtils.setField(handler, "appUri", "checkmo://oauth-callback");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute(
                AppleOAuth2AuthorizationRequestResolver.SESSION_CLIENT_TYPE,
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
                    AppleOAuth2AuthorizationRequestResolver.SESSION_CLIENT_TYPE)).isNull();
        });
    }
}
