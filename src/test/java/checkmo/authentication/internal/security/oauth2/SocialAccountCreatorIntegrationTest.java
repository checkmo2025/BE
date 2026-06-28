package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.entity.Role;
import checkmo.support.ApiTestSupport;
import jakarta.persistence.PersistenceException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SocialAccountCreatorIntegrationTest extends ApiTestSupport {

    @Autowired
    private SocialAccountCreator socialAccountCreator;

    @Test
    void persistsNewSocialUserWithAssignedProviderId() {
        OAuth2Attributes attributes = OAuth2Attributes.of("apple", Map.of(
                "sub", "apple-sub",
                "email", "apple-user@example.com"
        ));

        AuthUser createdUser = socialAccountCreator.create(attributes, "apple");
        AuthUser savedUser = authRepository.findById("APPLE_apple-sub").orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(createdUser.getId()).isEqualTo("APPLE_apple-sub");
            softly.assertThat(savedUser.getId()).isEqualTo("APPLE_apple-sub");
            softly.assertThat(savedUser.getEmail()).isEqualTo("apple-user@example.com");
            softly.assertThat(savedUser.isProfileCompleted()).isFalse();
        });
    }

    @Test
    void duplicateProviderIdFailsInsteadOfMergingExistingUser() {
        authRepository.saveAndFlush(user("APPLE_apple-sub", "existing@example.com"));
        OAuth2Attributes attributes = OAuth2Attributes.of("apple", Map.of(
                "sub", "apple-sub",
                "email", "new@example.com"
        ));

        assertThatThrownBy(() -> socialAccountCreator.create(attributes, "apple"))
                .isInstanceOf(PersistenceException.class);

        AuthUser savedUser = authRepository.findById("APPLE_apple-sub").orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(savedUser.getEmail()).isEqualTo("existing@example.com");
            softly.assertThat(authRepository.findByEmail("new@example.com")).isEmpty();
        });
    }

    private AuthUser user(String id, String email) {
        return AuthUser.builder()
                .id(id)
                .email(email)
                .password("")
                .role(Role.USER)
                .profileCompleted(false)
                .build();
    }
}
