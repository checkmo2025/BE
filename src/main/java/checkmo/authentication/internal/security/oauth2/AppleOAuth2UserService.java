package checkmo.authentication.internal.security.oauth2;

import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.security.apple.AppleIdTokenVerifier;
import checkmo.authentication.internal.security.apple.AppleIdentity;
import checkmo.authentication.internal.security.apple.InvalidAppleIdentityTokenException;
import checkmo.authentication.internal.security.auth.PrincipalDetails;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AppleOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String INVALID_APPLE_ID_TOKEN = "invalid_apple_id_token";

    private final AppleIdTokenVerifier appleIdTokenVerifier;
    private final SocialAccountResolver socialAccountResolver;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        AppleIdentity identity = verifyIdentity(userRequest);
        Map<String, Object> attributes = identity.toOAuth2Attributes();
        OAuth2Attributes oAuth2Attributes = OAuth2Attributes.of("apple", attributes);

        try {
            SocialAccountResolution result = socialAccountResolver.resolve(oAuth2Attributes, "apple");
            return new PrincipalDetails(result.user(), attributes, result.newSocialSignUp());
        } catch (AuthException e) {
            String code = e.getErrorReasonHttpStatus().getCode();
            String message = e.getErrorReasonHttpStatus().getMessage();
            throw new OAuth2AuthenticationException(new OAuth2Error(code, message, null), message, e);
        }
    }

    private AppleIdentity verifyIdentity(OAuth2UserRequest userRequest) {
        Object idToken = userRequest.getAdditionalParameters().get(OidcParameterNames.ID_TOKEN);
        if (!(idToken instanceof String value) || !StringUtils.hasText(value)) {
            throw invalidAppleIdToken(null);
        }

        try {
            return appleIdTokenVerifier.verifyWebToken(value);
        } catch (InvalidAppleIdentityTokenException e) {
            throw invalidAppleIdToken(e);
        }
    }

    private OAuth2AuthenticationException invalidAppleIdToken(Throwable cause) {
        OAuth2Error error = new OAuth2Error(INVALID_APPLE_ID_TOKEN, "Invalid Apple identity token", null);
        return new OAuth2AuthenticationException(error, error.getDescription(), cause);
    }
}
