package checkmo.authentication.internal.security.oauth2;

import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.security.apple.AppleIdTokenVerifier;
import checkmo.authentication.internal.security.apple.AppleIdentity;
import checkmo.authentication.internal.security.apple.InvalidAppleIdentityTokenException;
import checkmo.authentication.internal.security.auth.PrincipalDetails;
import checkmo.authentication.internal.security.jwt.JwtLoginProcessor;
import checkmo.authentication.web.dto.AuthRequestDTO;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AppleAppLoginService {

    private static final String REGISTRATION_ID = "apple";
    private static final String EMAIL_VERIFIED = "email_verified";
    private static final String PRIVATE_EMAIL = "is_private_email";

    private final AppleIdTokenVerifier appleIdTokenVerifier;
    private final SocialAccountResolver socialAccountResolver;
    private final JwtLoginProcessor jwtLoginProcessor;

    @Transactional
    public String login(AuthRequestDTO.AppleAppLogin request, HttpServletResponse response) {
        AppleIdentity identity = verifyIdentity(request);
        Map<String, Object> attributes = toAttributes(identity);
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
        return jwtLoginProcessor.processLogin(response, authentication);
    }

    private AppleIdentity verifyIdentity(AuthRequestDTO.AppleAppLogin request) {
        try {
            return appleIdTokenVerifier.verifyIosToken(request.getIdentityToken(), request.getRawNonce());
        } catch (InvalidAppleIdentityTokenException e) {
            throw new AuthException(AuthErrorStatus.APPLE_INVALID_TOKEN);
        }
    }

    private Map<String, Object> toAttributes(AppleIdentity identity) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("sub", identity.subject());
        if (StringUtils.hasText(identity.email())) {
            attributes.put("email", identity.email());
        }
        attributes.put(EMAIL_VERIFIED, identity.emailVerified());
        attributes.put(PRIVATE_EMAIL, identity.privateEmail());
        return Map.copyOf(attributes);
    }
}
