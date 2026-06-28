package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.entity.Role;
import checkmo.authentication.internal.security.apple.AppleIdTokenVerifier;
import checkmo.authentication.internal.security.apple.AppleIdentity;
import checkmo.authentication.internal.security.auth.PrincipalDetails;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

class AppleOAuth2UserServiceTest {

    @Test
    void loadsAppleUserFromIdTokenAndResolvesSocialAccount() {
        AppleIdTokenVerifier verifier = mock(AppleIdTokenVerifier.class);
        SocialAccountResolver socialAccountResolver = mock(SocialAccountResolver.class);
        AppleOAuth2UserService service = new AppleOAuth2UserService(verifier, socialAccountResolver);
        AppleIdentity identity = new AppleIdentity(
                "apple-sub",
                "APPLE_apple-sub",
                "apple-user@example.com",
                true,
                false,
                "kr.co.checkmo.web"
        );
        AuthUser user = user("APPLE_apple-sub", "apple-user@example.com");
        when(verifier.verifyWebToken("apple.identity.token")).thenReturn(identity);
        when(socialAccountResolver.resolve(any(OAuth2Attributes.class), eq("apple")))
                .thenReturn(new SocialAccountResolution(user, true));

        OAuth2User oauth2User = service.loadUser(appleUserRequest("apple.identity.token"));

        ArgumentCaptor<OAuth2Attributes> attributesCaptor = ArgumentCaptor.forClass(OAuth2Attributes.class);
        verify(socialAccountResolver).resolve(attributesCaptor.capture(), eq("apple"));
        OAuth2Attributes attributes = attributesCaptor.getValue();
        PrincipalDetails principalDetails = (PrincipalDetails) oauth2User;
        assertSoftly(softly -> {
            softly.assertThat(attributes.getProviderId()).isEqualTo("apple-sub");
            softly.assertThat(attributes.getEmail()).isEqualTo("apple-user@example.com");
            softly.assertThat(principalDetails.getUsername()).isEqualTo("APPLE_apple-sub");
            softly.assertThat(principalDetails.isNewSocialSignUp()).isTrue();
            softly.assertThat(principalDetails.getAttributes())
                    .containsEntry("sub", "apple-sub")
                    .containsEntry("email", "apple-user@example.com")
                    .containsEntry("email_verified", true)
                    .containsEntry("is_private_email", false);
        });
    }

    @Test
    void rejectsAppleUserRequestWithoutIdToken() {
        AppleIdTokenVerifier verifier = mock(AppleIdTokenVerifier.class);
        SocialAccountResolver socialAccountResolver = mock(SocialAccountResolver.class);
        AppleOAuth2UserService service = new AppleOAuth2UserService(verifier, socialAccountResolver);

        Throwable thrown = catchThrowable(() -> service.loadUser(appleUserRequest(null)));

        assertSoftly(softly -> {
            softly.assertThat(thrown).isInstanceOf(OAuth2AuthenticationException.class);
            OAuth2AuthenticationException exception = (OAuth2AuthenticationException) thrown;
            softly.assertThat(exception.getError().getErrorCode()).isEqualTo("invalid_apple_id_token");
        });
        verify(verifier, never()).verifyWebToken(any());
        verify(socialAccountResolver, never()).resolve(any(), eq("apple"));
    }

    private OAuth2UserRequest appleUserRequest(String idToken) {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token",
                Instant.parse("2026-06-28T00:00:00Z"),
                Instant.parse("2026-06-28T00:05:00Z")
        );
        Map<String, Object> additionalParameters = idToken == null
                ? Map.of()
                : Map.of("id_token", idToken);
        return new OAuth2UserRequest(appleRegistration(), accessToken, additionalParameters);
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

    private AuthUser user(String id, String email) {
        return AuthUser.builder()
                .id(id)
                .email(email)
                .password("")
                .role(Role.USER)
                .profileCompleted(false)
                .build();
    }
}
