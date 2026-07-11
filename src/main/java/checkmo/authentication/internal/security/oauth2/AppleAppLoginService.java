package checkmo.authentication.internal.security.oauth2;

import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.security.apple.AppleIdTokenVerifier;
import checkmo.authentication.internal.security.apple.AppleIdentity;
import checkmo.authentication.internal.security.apple.InvalidAppleIdentityTokenException;
import checkmo.authentication.internal.security.auth.PrincipalDetails;
import checkmo.authentication.internal.security.jwt.JwtLoginProcessor;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppleAppLoginService {

    private static final String REGISTRATION_ID = "apple";

    private final AppleIdTokenVerifier appleIdTokenVerifier;
    private final SocialAccountResolver socialAccountResolver;
    private final JwtLoginProcessor jwtLoginProcessor;

    @Transactional
    public String login(String identityToken, String rawNonce, HttpServletResponse response) {
        AppleIdentity identity = verifyIdentity(identityToken, rawNonce);
        Map<String, Object> attributes = identity.toOAuth2Attributes();
        SocialAccountResolution result = socialAccountResolver.resolve(
                OAuth2Attributes.of(REGISTRATION_ID, attributes),
                REGISTRATION_ID
        );

        PrincipalDetails principalDetails = new PrincipalDetails(
                result.user(),
                attributes,
                result.newSocialSignUp()
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principalDetails,
                null,
                principalDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtLoginProcessor.processAppLogin(response, authentication);
    }

    private AppleIdentity verifyIdentity(String identityToken, String rawNonce) {
        try {
            return appleIdTokenVerifier.verifyIosToken(identityToken, rawNonce);
        } catch (InvalidAppleIdentityTokenException e) {
            throw new AuthException(AuthErrorStatus.APPLE_INVALID_TOKEN);
        }
    }
}
