package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.entity.Role;
import checkmo.authentication.internal.security.auth.PrincipalDetails;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class AppleOidcUserServiceTest {

    @Test
    void adaptsApplePrincipalDetailsToOidcUser() {
        AppleOAuth2UserService appleOAuth2UserService = mock(AppleOAuth2UserService.class);
        AppleOidcUserService appleOidcUserService = new AppleOidcUserService(appleOAuth2UserService);
        OidcUserRequest userRequest = userRequest();
        PrincipalDetails principalDetails = new PrincipalDetails(
                user(),
                Map.of("sub", "apple-sub", "email", "apple-user@example.com"),
                true
        );
        when(appleOAuth2UserService.loadUser(userRequest)).thenReturn(principalDetails);

        OidcUser oidcUser = appleOidcUserService.loadUser(userRequest);

        assertSoftly(softly -> {
            softly.assertThat(oidcUser).isInstanceOf(PrincipalDetails.class);
            softly.assertThat(((PrincipalDetails) oidcUser).getUser().getId()).isEqualTo("APPLE_apple-sub");
            softly.assertThat(((PrincipalDetails) oidcUser).isNewSocialSignUp()).isTrue();
            softly.assertThat(oidcUser.getIdToken()).isSameAs(userRequest.getIdToken());
            softly.assertThat(oidcUser.getClaims()).containsEntry("sub", "apple-sub");
        });
        verify(appleOAuth2UserService).loadUser(userRequest);
    }

    private OidcUserRequest userRequest() {
        Instant issuedAt = Instant.parse("2026-07-01T00:00:00Z");
        Instant expiresAt = Instant.parse("2026-07-01T00:05:00Z");
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token",
                issuedAt,
                expiresAt
        );
        OidcIdToken idToken = new OidcIdToken(
                "id-token",
                issuedAt,
                expiresAt,
                Map.of(
                        IdTokenClaimNames.ISS, "https://appleid.apple.com",
                        IdTokenClaimNames.SUB, "apple-sub",
                        IdTokenClaimNames.AUD, "kr.co.checkmo.web"
                )
        );
        return new OidcUserRequest(appleRegistration(), accessToken, idToken, Map.of());
    }

    private ClientRegistration appleRegistration() {
        return ClientRegistration.withRegistrationId("apple")
                .clientId("kr.co.checkmo.web")
                .clientSecret("client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://api.checkmo.co.kr/login/oauth2/code/apple")
                .authorizationUri("https://appleid.apple.com/auth/authorize")
                .tokenUri("https://appleid.apple.com/auth/token")
                .jwkSetUri("https://appleid.apple.com/auth/keys")
                .scope("openid", "email", "name")
                .clientName("Apple")
                .build();
    }

    private AuthUser user() {
        return AuthUser.builder()
                .id("APPLE_apple-sub")
                .email("apple-user@example.com")
                .password("")
                .role(Role.USER)
                .profileCompleted(false)
                .build();
    }
}
