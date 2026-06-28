package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

class SocialOAuth2UserServiceTest {

    @Test
    void delegatesAppleRegistrationToAppleUserService() {
        CustomOAuth2UserService defaultOAuth2UserService = mock(CustomOAuth2UserService.class);
        AppleOAuth2UserService appleOAuth2UserService = mock(AppleOAuth2UserService.class);
        SocialOAuth2UserService service =
                new SocialOAuth2UserService(defaultOAuth2UserService, appleOAuth2UserService);
        OAuth2UserRequest userRequest = userRequest("apple");
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(appleOAuth2UserService.loadUser(userRequest)).thenReturn(oauth2User);

        OAuth2User result = service.loadUser(userRequest);

        assertThat(result).isSameAs(oauth2User);
        verify(appleOAuth2UserService).loadUser(userRequest);
        verify(defaultOAuth2UserService, never()).loadUser(userRequest);
    }

    @Test
    void delegatesNonAppleRegistrationToDefaultUserService() {
        CustomOAuth2UserService defaultOAuth2UserService = mock(CustomOAuth2UserService.class);
        AppleOAuth2UserService appleOAuth2UserService = mock(AppleOAuth2UserService.class);
        SocialOAuth2UserService service =
                new SocialOAuth2UserService(defaultOAuth2UserService, appleOAuth2UserService);
        OAuth2UserRequest userRequest = userRequest("google");
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(defaultOAuth2UserService.loadUser(userRequest)).thenReturn(oauth2User);

        OAuth2User result = service.loadUser(userRequest);

        assertThat(result).isSameAs(oauth2User);
        verify(defaultOAuth2UserService).loadUser(userRequest);
        verify(appleOAuth2UserService, never()).loadUser(userRequest);
    }

    private OAuth2UserRequest userRequest(String registrationId) {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token",
                Instant.parse("2026-06-28T00:00:00Z"),
                Instant.parse("2026-06-28T00:05:00Z")
        );
        return new OAuth2UserRequest(clientRegistration(registrationId), accessToken, Map.of());
    }

    private ClientRegistration clientRegistration(String registrationId) {
        return ClientRegistration.withRegistrationId(registrationId)
                .clientId(registrationId + "-client-id")
                .clientSecret(registrationId + "-client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://api.checkmo.co.kr/login/oauth2/code/" + registrationId)
                .authorizationUri("https://example.com/oauth2/authorize")
                .tokenUri("https://example.com/oauth2/token")
                .clientName(registrationId)
                .build();
    }
}
