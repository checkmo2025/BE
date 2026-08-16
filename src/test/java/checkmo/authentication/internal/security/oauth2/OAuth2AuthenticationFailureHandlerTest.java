package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
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

        handler.onAuthenticationFailure(request, response, exception);

        assertSoftly(softly -> {
            softly.assertThat(response.getRedirectedUrl())
                    .isEqualTo("https://web.checkmo.test/?error=login_failed");
            softly.assertThat(response.getRedirectedUrl()).doesNotContain("secret-value");
            softly.assertThat(response.getRedirectedUrl()).doesNotContain("token=");
        });
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

    @Test
    void retriesKakaoEmailConsentOnce() throws Exception {
        OAuth2AuthenticationFailureHandler handler = new OAuth2AuthenticationFailureHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/login/oauth2/code/kakao");
        request.getSession();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(
                request,
                response,
                oauthException(OAuth2ErrorCodes.KAKAO_EMAIL_CONSENT_REQUIRED)
        );

        assertSoftly(softly -> {
            softly.assertThat(response.getRedirectedUrl()).isEqualTo("/oauth2/authorization/kakao");
            softly.assertThat(request.getSession().getAttribute(KakaoEmailConsentRetryState.SESSION_ATTRIBUTE))
                    .isEqualTo(Boolean.TRUE);
        });
    }

    @Test
    void redirectsNormallyWhenKakaoEmailConsentRetryIsExhausted() throws Exception {
        OAuth2AuthenticationFailureHandler handler = new OAuth2AuthenticationFailureHandler();
        ReflectionTestUtils.setField(handler, "baseUri", "https://web.checkmo.test/");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/login/oauth2/code/kakao");
        request.getSession().setAttribute(KakaoEmailConsentRetryState.SESSION_ATTRIBUTE, Boolean.TRUE);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(
                request,
                response,
                oauthException(OAuth2ErrorCodes.KAKAO_EMAIL_CONSENT_REQUIRED)
        );

        assertSoftly(softly -> {
            softly.assertThat(response.getRedirectedUrl())
                    .isEqualTo("https://web.checkmo.test/?error=login_failed");
            softly.assertThat(request.getSession().getAttribute(KakaoEmailConsentRetryState.SESSION_ATTRIBUTE))
                    .isNull();
        });
    }

    private OAuth2AuthenticationException oauthException(String errorCode) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(errorCode, "controlled oauth failure", null),
                "controlled oauth failure"
        );
    }
}
