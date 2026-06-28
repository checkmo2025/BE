package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

class AppleOAuth2AuthorizationRequestResolverTest {

    @Test
    void addsFormPostResponseModeForAppleAuthorizationRequest() {
        AppleOAuth2AuthorizationRequestResolver resolver = new AppleOAuth2AuthorizationRequestResolver(
                new InMemoryClientRegistrationRepository(appleRegistration())
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/apple");
        request.setServerName("api.checkmo.co.kr");
        request.setScheme("https");
        request.setServerPort(443);

        OAuth2AuthorizationRequest authorizationRequest = resolver.resolve(request);

        assertSoftly(softly -> {
            softly.assertThat(authorizationRequest.getAdditionalParameters())
                    .containsEntry("response_mode", "form_post");
            softly.assertThat(authorizationRequest.getAuthorizationRequestUri())
                    .contains("response_mode=form_post")
                    .contains("client_id=kr.co.checkmo.web");
        });
    }

    @Test
    void storesAppClientTypeAndKeepsFormPostForAppleAppAuthorizationRequest() {
        AppleOAuth2AuthorizationRequestResolver resolver = new AppleOAuth2AuthorizationRequestResolver(
                new InMemoryClientRegistrationRepository(appleRegistration())
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/apple");
        request.setParameter("client", "app");
        request.setServerName("api.checkmo.co.kr");
        request.setScheme("https");
        request.setServerPort(443);

        OAuth2AuthorizationRequest authorizationRequest = resolver.resolve(request);

        assertSoftly(softly -> {
            softly.assertThat(authorizationRequest.getAdditionalParameters())
                    .containsEntry("response_mode", "form_post");
            softly.assertThat(authorizationRequest.getAuthorizationRequestUri())
                    .contains("response_mode=form_post")
                    .contains("client_id=kr.co.checkmo.web");
            softly.assertThat(request.getSession().getAttribute(
                            AppleOAuth2AuthorizationRequestResolver.SESSION_CLIENT_TYPE))
                    .isEqualTo(AppleOAuth2AuthorizationRequestResolver.CLIENT_TYPE_APP);
        });
    }

    @Test
    void storesAppClientTypeWithoutFormPostForNonAppleAppAuthorizationRequest() {
        AppleOAuth2AuthorizationRequestResolver resolver = new AppleOAuth2AuthorizationRequestResolver(
                new InMemoryClientRegistrationRepository(googleRegistration())
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/google");
        request.setParameter("client", "app");

        OAuth2AuthorizationRequest authorizationRequest = resolver.resolve(request);

        assertSoftly(softly -> {
            softly.assertThat(authorizationRequest.getAdditionalParameters()).doesNotContainKey("response_mode");
            softly.assertThat(authorizationRequest.getAuthorizationRequestUri()).doesNotContain("response_mode");
            softly.assertThat(request.getSession().getAttribute(
                            AppleOAuth2AuthorizationRequestResolver.SESSION_CLIENT_TYPE))
                    .isEqualTo(AppleOAuth2AuthorizationRequestResolver.CLIENT_TYPE_APP);
        });
    }

    @Test
    void clearsStaleAppClientTypeForWebAuthorizationRequest() {
        AppleOAuth2AuthorizationRequestResolver resolver = new AppleOAuth2AuthorizationRequestResolver(
                new InMemoryClientRegistrationRepository(googleRegistration())
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/google");
        request.getSession().setAttribute(
                AppleOAuth2AuthorizationRequestResolver.SESSION_CLIENT_TYPE,
                AppleOAuth2AuthorizationRequestResolver.CLIENT_TYPE_APP
        );

        OAuth2AuthorizationRequest authorizationRequest = resolver.resolve(request);

        assertSoftly(softly -> {
            softly.assertThat(authorizationRequest.getAuthorizationRequestUri()).doesNotContain("response_mode");
            softly.assertThat(request.getSession().getAttribute(
                    AppleOAuth2AuthorizationRequestResolver.SESSION_CLIENT_TYPE)).isNull();
        });
    }

    @Test
    void keepsDefaultParametersForNonAppleAuthorizationRequest() {
        AppleOAuth2AuthorizationRequestResolver resolver = new AppleOAuth2AuthorizationRequestResolver(
                new InMemoryClientRegistrationRepository(googleRegistration())
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/google");

        OAuth2AuthorizationRequest authorizationRequest = resolver.resolve(request);

        assertSoftly(softly -> {
            softly.assertThat(authorizationRequest.getAdditionalParameters()).doesNotContainKey("response_mode");
            softly.assertThat(authorizationRequest.getAuthorizationRequestUri()).doesNotContain("response_mode");
        });
    }

    private ClientRegistration appleRegistration() {
        return ClientRegistration.withRegistrationId("apple")
                .clientId("kr.co.checkmo.web")
                .clientSecret("placeholder")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://api.checkmo.co.kr/login/oauth2/code/apple")
                .authorizationUri("https://appleid.apple.com/auth/authorize")
                .tokenUri("https://appleid.apple.com/auth/token")
                .scope("openid", "email", "name")
                .clientName("Apple")
                .build();
    }

    private ClientRegistration googleRegistration() {
        return ClientRegistration.withRegistrationId("google")
                .clientId("google-client-id")
                .clientSecret("google-client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://api.checkmo.co.kr/login/oauth2/code/google")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .scope("email", "profile")
                .clientName("Google")
                .build();
    }
}
