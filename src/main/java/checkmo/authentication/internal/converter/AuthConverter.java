package checkmo.authentication.internal.converter;

import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.entity.Role;
import checkmo.authentication.internal.security.oauth2.OAuth2Attributes;
import checkmo.authentication.internal.service.result.AuthSignUpResult;
import checkmo.authentication.web.dto.AuthRequestDTO;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthConverter {

    private static final String LOCAL_PROVIDER = "LOCAL";

    public static AuthSignUpResult toSignUpResult(AuthUser user) {
        return new AuthSignUpResult(user.getEmail(), user.isProfileCompleted());
    }

    public static AuthUser toOAuth2User(OAuth2Attributes attributes, String registrationId) {
        String provider = registrationId.toUpperCase();

        return AuthUser.builder()
                .legacyId(toLegacyId(provider, attributes.getProviderId()))
                .email(attributes.getEmail())
                .password("")
                .provider(provider)
                .providerUserId(attributes.getProviderId())
                .role(Role.USER)
                .deactivatedAt(null)
                .profileCompleted(false)
                .build();
    }

    public static String toLegacyId(String provider, String providerUserId) {
        return provider.toUpperCase() + "_" + providerUserId;
    }

    public static AuthUser toLocalUser(AuthRequestDTO.SignUp request, String encodedPassword) {
        String providerUserId = UUID.randomUUID().toString().substring(0, 8);

        return AuthUser.builder()
                .legacyId(toLegacyId(LOCAL_PROVIDER, providerUserId))
                .email(request.getEmail())
                .password(encodedPassword)
                .provider(LOCAL_PROVIDER)
                .providerUserId(providerUserId)
                .role(Role.USER)
                .deactivatedAt(null)
                .profileCompleted(false)
                .build();
    }
}
