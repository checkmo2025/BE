package checkmo.authentication.internal.config;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import checkmo.book.internal.scheduler.BookRecommendationScheduler;
import checkmo.bookStory.internal.scheduler.BookStoryViewScheduler;
import checkmo.member.internal.scheduler.MemberCleanupScheduler;
import checkmo.support.SpringTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringTest
@ActiveProfiles({"prod", "test"})
class OAuthSessionCookiePropertiesTest {

    @Autowired
    private Environment environment;

    @MockitoBean
    private BookRecommendationScheduler bookRecommendationScheduler;

    @MockitoBean
    private BookStoryViewScheduler bookStoryViewScheduler;

    @MockitoBean
    private MemberCleanupScheduler memberCleanupScheduler;

    @Test
    void configuresSecureCrossSiteSessionCookieForAppleFormPostCallback() {
        assertSoftly(softly -> {
            softly.assertThat(environment.getProperty("server.servlet.session.cookie.same-site"))
                    .isEqualTo("none");
            softly.assertThat(environment.getProperty("server.servlet.session.cookie.secure"))
                    .isEqualTo("true");
        });
    }
}
