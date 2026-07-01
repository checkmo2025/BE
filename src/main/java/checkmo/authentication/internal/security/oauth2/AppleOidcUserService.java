package checkmo.authentication.internal.security.oauth2;

import checkmo.authentication.internal.security.auth.PrincipalDetails;
import checkmo.authentication.internal.security.auth.PrincipalOidcDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppleOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final AppleOAuth2UserService appleOAuth2UserService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        PrincipalDetails principalDetails = (PrincipalDetails) appleOAuth2UserService.loadUser(userRequest);
        return new PrincipalOidcDetails(
                principalDetails.getUser(),
                principalDetails.getAttributes(),
                principalDetails.isNewSocialSignUp(),
                userRequest.getIdToken(),
                null
        );
    }
}
