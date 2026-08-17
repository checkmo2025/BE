package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.entity.Role;
import checkmo.authentication.internal.repository.AuthRepository;
import checkmo.book.internal.scheduler.BookRecommendationScheduler;
import checkmo.bookStory.internal.scheduler.BookStoryViewScheduler;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.Terms;
import checkmo.member.internal.entity.TermsType;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.internal.repository.TermsRepository;
import checkmo.member.internal.scheduler.MemberCleanupScheduler;
import checkmo.support.SpringTest;
import jakarta.persistence.PersistenceException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringTest
@TestPropertySource(properties = {
        "aladin.api.recommendation.refresh.background.fixed-delay=60000",
        "checkmo.terms.enforcement-enabled=true"
})
class SocialAccountCreatorIntegrationTest {

    @Autowired
    private SocialAccountCreator socialAccountCreator;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TermsRepository termsRepository;

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
    void persistsNewSocialUsersForEveryProviderWithoutNicknameIdentity() {
        termsRepository.save(Terms.builder()
                .termsType(TermsType.SERVICE_TERMS)
                .title("필수 서비스 약관")
                .termUrl("https://example.com/required-terms")
                .version(1)
                .active(true)
                .required(true)
                .build());

        List<SocialAccountFixture> fixtures = List.of(
                new SocialAccountFixture("google", "google-sub", "google-user@example.com", OAuth2Attributes.of(
                        "google",
                        Map.of("sub", "google-sub", "email", "google-user@example.com")
                )),
                new SocialAccountFixture("kakao", "12345", "kakao-user@example.com", OAuth2Attributes.of(
                        "kakao",
                        Map.of(
                                "id", 12345L,
                                "kakao_account", Map.of("email", "kakao-user@example.com")
                        )
                )),
                new SocialAccountFixture("naver", "naver-id", "naver-user@example.com", OAuth2Attributes.of(
                        "naver",
                        Map.of("response", Map.of(
                                "id", "naver-id",
                                "email", "naver-user@example.com"
                        ))
                )),
                new SocialAccountFixture("apple", "apple-sub", "apple-user@example.com", OAuth2Attributes.of(
                        "apple",
                        Map.of("sub", "apple-sub", "email", "apple-user@example.com")
                ))
        );

        fixtures.forEach(fixture -> socialAccountCreator.create(fixture.attributes(), fixture.registrationId()));

        assertThat(authRepository.count()).isEqualTo(fixtures.size());
        assertThat(memberRepository.count()).isEqualTo(fixtures.size());
        fixtures.forEach(this::assertIncompleteSocialAccountPersisted);
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

    private void assertIncompleteSocialAccountPersisted(SocialAccountFixture fixture) {
        String provider = fixture.registrationId().toUpperCase();
        AuthUser savedUser = authRepository
                .findByProviderAndProviderUserId(provider, fixture.providerUserId())
                .orElseThrow();
        Member savedMember = memberRepository.findById(savedUser.getId()).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(savedUser.getLegacyId()).isEqualTo(provider + "_" + fixture.providerUserId());
            softly.assertThat(savedUser.getEmail()).isEqualTo(fixture.email());
            softly.assertThat(savedUser.getNickName()).isNull();
            softly.assertThat(savedUser.getNickNameKey()).isNull();
            softly.assertThat(savedUser.isProfileCompleted()).isFalse();
            softly.assertThat(savedMember.getEmail()).isEqualTo(fixture.email());
            softly.assertThat(savedMember.getNickName()).isNull();
            softly.assertThat(savedMember.getNickNameKey()).isNull();
        });
    }

    private String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private record SocialAccountFixture(
            String registrationId,
            String providerUserId,
            String email,
            OAuth2Attributes attributes
    ) {
    }
}
