package checkmo.authentication.internal.security.oauth2;

import checkmo.authentication.internal.converter.AuthConverter;
import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.entity.Provider;
import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.repository.AuthRepository;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SocialAccountResolver {

    private final AuthRepository authRepository;
    private final SocialAccountCreator socialAccountCreator;

    public SocialAccountResolution resolve(OAuth2Attributes attributes, String registrationId) {
        if (Provider.APPLE.equalsIgnoreCase(registrationId)) {
            return resolveApple(attributes, registrationId);
        }

        return resolveByEmail(attributes, registrationId);
    }

    private SocialAccountResolution resolveApple(OAuth2Attributes attributes, String registrationId) {
        if (!StringUtils.hasText(attributes.getProviderId())) {
            throw new OAuth2AuthenticationException("소셜 계정 식별자를 가져올 수 없습니다");
        }
        String expectedUserId = AuthConverter.toOAuth2MemberId(registrationId, attributes.getProviderId());

        return authRepository.findById(expectedUserId)
                .map(user -> new SocialAccountResolution(user, false))
                .orElseGet(() -> createAppleUser(attributes, registrationId, expectedUserId));
    }

    private SocialAccountResolution createAppleUser(
            OAuth2Attributes attributes,
            String registrationId,
            String expectedUserId
    ) {
        String email = attributes.getEmail();
        if (!StringUtils.hasText(email)) {
            throw new AuthException(AuthErrorStatus.APPLE_EMAIL_REQUIRED);
        }

        authRepository.findByEmail(email).ifPresent(user -> {
            throw new AuthException(AuthErrorStatus.SOCIAL_ACCOUNT_EMAIL_CONFLICT);
        });

        try {
            AuthUser savedUser = socialAccountCreator.create(attributes, registrationId);
            return new SocialAccountResolution(savedUser, true);
        } catch (DataIntegrityViolationException | PersistenceException e) {
            return refetchAppleAfterDuplicate(expectedUserId, email);
        }
    }

    private SocialAccountResolution refetchAppleAfterDuplicate(String expectedUserId, String email) {
        AuthUser user = authRepository.findById(expectedUserId)
                .or(() -> authRepository.findByEmail(email).filter(foundUser -> foundUser.getId().equals(expectedUserId)))
                .orElseThrow(() -> new AuthException(AuthErrorStatus.SOCIAL_ACCOUNT_EMAIL_CONFLICT));

        return new SocialAccountResolution(user, false);
    }

    private SocialAccountResolution resolveByEmail(OAuth2Attributes attributes, String registrationId) {
        String email = attributes.getEmail();
        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException("소셜 계정에서 이메일 정보를 가져올 수 없습니다");
        }

        return authRepository.findByEmail(email)
                .map(user -> new SocialAccountResolution(user, false))
                .orElseGet(() -> new SocialAccountResolution(socialAccountCreator.create(attributes, registrationId), true));
    }
}

record SocialAccountResolution(AuthUser user, boolean newSocialSignUp) {
}
