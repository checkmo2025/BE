package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.authentication.internal.security.apple.AppleClientSecretGenerator;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.MultiValueMap;

class AppleClientSecretTokenRequestParametersConverterTest {

    @Test
    void replacesAppleClientSecretWithGeneratedValue() {
        AppleClientSecretGenerator generator = mock(AppleClientSecretGenerator.class);
        AppleClientSecretTokenRequestParametersConverter converter =
                new AppleClientSecretTokenRequestParametersConverter(generator);
        when(generator.generateClientSecret()).thenReturn("generated-apple-client-secret");

        MultiValueMap<String, String> parameters = converter.convert(grantRequest(appleRegistration()));

        assertSoftly(softly -> {
            softly.assertThat(parameters.getFirst(OAuth2ParameterNames.CLIENT_ID)).isEqualTo("kr.co.checkmo.web");
            softly.assertThat(parameters.getFirst(OAuth2ParameterNames.CLIENT_SECRET))
                    .isEqualTo("generated-apple-client-secret");
            softly.assertThat(parameters.getFirst(OAuth2ParameterNames.CODE)).isEqualTo("authorization-code");
        });
        verify(generator).generateClientSecret();
    }

    @Test
    void keepsDefaultClientSecretForNonAppleRegistration() {
        AppleClientSecretGenerator generator = mock(AppleClientSecretGenerator.class);
        AppleClientSecretTokenRequestParametersConverter converter =
                new AppleClientSecretTokenRequestParametersConverter(generator);

        MultiValueMap<String, String> parameters = converter.convert(grantRequest(googleRegistration()));

        assertSoftly(softly -> {
            softly.assertThat(parameters.getFirst(OAuth2ParameterNames.CLIENT_ID)).isEqualTo("google-client-id");
            softly.assertThat(parameters.getFirst(OAuth2ParameterNames.CLIENT_SECRET))
                    .isEqualTo("google-client-secret");
        });
        verify(generator, never()).generateClientSecret();
    }

    private OAuth2AuthorizationCodeGrantRequest grantRequest(ClientRegistration clientRegistration) {
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
                .clientId(clientRegistration.getClientId())
                .redirectUri(clientRegistration.getRedirectUri())
                .state("state")
                .attributes(Map.of(OAuth2ParameterNames.REGISTRATION_ID, clientRegistration.getRegistrationId()))
                .build();
        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse.success("authorization-code")
                .redirectUri(clientRegistration.getRedirectUri())
                .state("state")
                .build();
        return new OAuth2AuthorizationCodeGrantRequest(
                clientRegistration,
                new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse)
        );
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
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://api.checkmo.co.kr/login/oauth2/code/google")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .scope("email", "profile")
                .clientName("Google")
                .build();
    }
}
