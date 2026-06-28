package checkmo.authentication.internal.security.apple;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AppleIdentityTest {

    @Test
    void convertsToOAuth2Attributes() {
        AppleIdentity identity = new AppleIdentity(
                "apple-sub",
                "APPLE_apple-sub",
                "user@example.com",
                true,
                false,
                "kr.co.checkmo.web"
        );

        Map<String, Object> attributes = identity.toOAuth2Attributes();

        assertSoftly(softly -> {
            softly.assertThat(attributes.get("sub")).isEqualTo("apple-sub");
            softly.assertThat(attributes.get("email")).isEqualTo("user@example.com");
            softly.assertThat(attributes.get("email_verified")).isEqualTo(true);
            softly.assertThat(attributes.get("is_private_email")).isEqualTo(false);
        });
    }

    @Test
    void omitsEmailWhenEmailIsBlank() {
        AppleIdentity identity = new AppleIdentity(
                "apple-sub",
                "APPLE_apple-sub",
                "   ",
                false,
                true,
                "kr.co.checkmo.web"
        );

        Map<String, Object> attributes = identity.toOAuth2Attributes();

        assertSoftly(softly -> {
            softly.assertThat(attributes).doesNotContainKey("email");
            softly.assertThat(attributes.get("sub")).isEqualTo("apple-sub");
            softly.assertThat(attributes.get("email_verified")).isEqualTo(false);
            softly.assertThat(attributes.get("is_private_email")).isEqualTo(true);
        });
    }

    @Test
    void omitsEmailWhenEmailIsNull() {
        AppleIdentity identity = new AppleIdentity(
                "apple-sub",
                "APPLE_apple-sub",
                null,
                false,
                true,
                "kr.co.checkmo.web"
        );

        Map<String, Object> attributes = identity.toOAuth2Attributes();

        assertSoftly(softly -> {
            softly.assertThat(attributes).doesNotContainKey("email");
            softly.assertThat(attributes.get("sub")).isEqualTo("apple-sub");
            softly.assertThat(attributes.get("email_verified")).isEqualTo(false);
            softly.assertThat(attributes.get("is_private_email")).isEqualTo(true);
        });
    }
}
