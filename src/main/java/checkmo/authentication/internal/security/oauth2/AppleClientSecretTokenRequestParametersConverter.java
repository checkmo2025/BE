package checkmo.authentication.internal.security.oauth2;

import checkmo.authentication.internal.entity.Provider;
import checkmo.authentication.internal.security.apple.AppleClientSecretGenerator;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestParametersConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.MultiValueMap;

public class AppleClientSecretTokenRequestParametersConverter
        implements Converter<OAuth2AuthorizationCodeGrantRequest, MultiValueMap<String, String>> {

    private final DefaultOAuth2TokenRequestParametersConverter<OAuth2AuthorizationCodeGrantRequest> delegate =
            new DefaultOAuth2TokenRequestParametersConverter<>();
    private final AppleClientSecretGenerator appleClientSecretGenerator;

    public AppleClientSecretTokenRequestParametersConverter(
            AppleClientSecretGenerator appleClientSecretGenerator
    ) {
        this.appleClientSecretGenerator = appleClientSecretGenerator;
    }

    @Override
    public MultiValueMap<String, String> convert(OAuth2AuthorizationCodeGrantRequest grantRequest) {
        MultiValueMap<String, String> parameters = delegate.convert(grantRequest);
        if (Provider.APPLE.equals(grantRequest.getClientRegistration().getRegistrationId())) {
            parameters.set(OAuth2ParameterNames.CLIENT_SECRET, appleClientSecretGenerator.generateClientSecret());
        }
        return parameters;
    }
}
