package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.entity.Role;
import checkmo.authentication.internal.repository.AuthRepository;
import checkmo.book.internal.scheduler.BookRecommendationScheduler;
import checkmo.bookStory.internal.scheduler.BookStoryViewScheduler;
import checkmo.member.internal.scheduler.MemberCleanupScheduler;
import checkmo.support.SpringTest;
import jakarta.persistence.PersistenceException;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringTest
@TestPropertySource(properties = "aladin.api.recommendation.refresh.background.fixed-delay=60000")
class SocialAccountCreatorIntegrationTest {

    @Autowired
    private SocialAccountCreator socialAccountCreator;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private BookRecommendationScheduler bookRecommendationScheduler;

    @MockitoBean
    private BookStoryViewScheduler bookStoryViewScheduler;

    @MockitoBean
    private MemberCleanupScheduler memberCleanupScheduler;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.queryForList(
                        """
                                select table_name
                                from information_schema.tables
                                where lower(table_schema) = 'public'
                                  and table_type in ('BASE TABLE', 'TABLE')
                                """,
                        String.class
                )
                .forEach(tableName -> jdbcTemplate.execute("delete from " + quoteIdentifier(tableName)));
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    @Test
    void persistsNewSocialUserWithAssignedProviderId() {
        OAuth2Attributes attributes = OAuth2Attributes.of("apple", Map.of(
                "sub", "apple-sub",
                "email", "apple-user@example.com"
        ));

        AuthUser createdUser = socialAccountCreator.create(attributes, "apple");
        AuthUser savedUser = authRepository.findByProviderAndProviderUserId("APPLE", "apple-sub").orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(createdUser.getLegacyId()).isEqualTo("APPLE_apple-sub");
            softly.assertThat(savedUser.getLegacyId()).isEqualTo("APPLE_apple-sub");
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

        AuthUser savedUser = authRepository.findByProviderAndProviderUserId("APPLE", "apple-sub").orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(savedUser.getEmail()).isEqualTo("existing@example.com");
            softly.assertThat(authRepository.findByEmail("new@example.com")).isEmpty();
        });
    }

    private AuthUser user(String id, String email) {
        String provider = id.substring(0, id.indexOf("_"));
        String providerUserId = id.substring(id.indexOf("_") + 1);
        return AuthUser.builder()
                .legacyId(id)
                .email(email)
                .password("")
                .provider(provider)
                .providerUserId(providerUserId)
                .role(Role.USER)
                .profileCompleted(false)
                .build();
    }

    private String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
