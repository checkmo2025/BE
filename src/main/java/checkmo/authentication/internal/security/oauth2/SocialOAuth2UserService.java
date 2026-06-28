package checkmo.authentication.internal.security.oauth2;

import checkmo.authentication.internal.entity.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final CustomOAuth2UserService defaultOAuth2UserService;
    private final AppleOAuth2UserService appleOAuth2UserService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (Provider.APPLE.equals(registrationId)) {
            return appleOAuth2UserService.loadUser(userRequest);
        }
        return defaultOAuth2UserService.loadUser(userRequest);
    }
}
