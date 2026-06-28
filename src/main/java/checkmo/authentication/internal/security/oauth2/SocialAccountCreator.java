package checkmo.authentication.internal.security.oauth2;

import checkmo.authentication.AuthenticationEvent;
import checkmo.authentication.internal.converter.AuthConverter;
import checkmo.authentication.internal.entity.AuthUser;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class SocialAccountCreator {

    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuthUser create(OAuth2Attributes attributes, String registrationId) {
        AuthUser newUser = AuthConverter.toOAuth2User(attributes, registrationId);
        entityManager.persist(newUser);
        entityManager.flush();

        eventPublisher.publishEvent(
                AuthenticationEvent.CreateMember.builder()
                        .id(newUser.getId())
                        .email(newUser.getEmail())
                        .build());

        return newUser;
    }
}
