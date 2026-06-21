package checkmo.book.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RecommendationLogSanitizerTest {

    private static final String SECRET = "SECRET";
    private static final String ACCESS_TOKEN = "ACCESS_SECRET";
    private static final String REFRESH_TOKEN = "REFRESH_SECRET";
    private static final String AUTHORIZATION = "AUTH_SECRET";
    private static final String API_KEY = "KEY_SECRET";
    private static final String CLIENT_SECRET = "CLIENT_SECRET";
    private static final String CLIENT_SECRET_WITH_SPACES = "CLIENT SECRET PLACEHOLDER";

    @Test
    void sanitizeReturnsNullWhenInputIsNull() {
        String sanitized = RecommendationLogSanitizer.sanitize(null);

        assertThat(sanitized).isNull();
    }

    @Test
    void sanitizeMasksUrlQueryParameters() {
        String sanitized = RecommendationLogSanitizer.sanitize(
                "refresh failed https://example.test/token?access_token=ACCESS_SECRET&key=KEY_SECRET status=kept"
        );

        assertThat(sanitized)
                .isEqualTo("refresh failed https://example.test/token?access_token=***&key=*** status=kept")
                .doesNotContain(ACCESS_TOKEN)
                .doesNotContain(API_KEY);
    }

    @Test
    void sanitizeTruncatesLongMessagesAfterRedaction() {
        String sanitized = RecommendationLogSanitizer.sanitize("a".repeat(501));

        assertThat(sanitized).isEqualTo("a".repeat(500) + "...");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("jsonCredentialFieldCases")
    void sanitizeRedactsQuotedJsonCredentialFieldsAndPreservesUnrelatedFields(
            String displayName,
            String raw,
            String expected
    ) {
        String sanitized = RecommendationLogSanitizer.sanitize(raw);

        assertThat(sanitized).isEqualTo(expected);
        assertThat(sanitized)
                .doesNotContain(ACCESS_TOKEN)
                .doesNotContain(REFRESH_TOKEN)
                .doesNotContain(AUTHORIZATION)
                .doesNotContain(API_KEY)
                .doesNotContain(CLIENT_SECRET)
                .doesNotContain(CLIENT_SECRET_WITH_SPACES)
                .contains("\"status\":\"kept\"")
                .contains("\"count\":1");
    }

    @Test
    void sanitizeRedactsMalformedUnterminatedQuotedJsonCredentialValue() {
        String sanitized = RecommendationLogSanitizer.sanitize(
                "refresh failed {\"secret\":\"CLIENT SECRET PLACEHOLDER,status=kept"
        );

        assertThat(sanitized)
                .isEqualTo("refresh failed {\"secret\":\"***,status=kept")
                .doesNotContain(CLIENT_SECRET_WITH_SPACES)
                .doesNotContain("SECRET PLACEHOLDER")
                .contains("status=kept");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sensitiveFieldCases")
    void sanitizeRedactsCredentialFieldsOutsideUrlQueryParameters(String displayName, String raw, String expected) {
        String sanitized = RecommendationLogSanitizer.sanitize(raw);

        assertThat(sanitized).isEqualTo(expected);
        assertThat(sanitized).doesNotContain(SECRET);
    }

    private static Stream<Arguments> sensitiveFieldCases() {
        return Stream.of(
                Arguments.of("auth assignment", "refresh failed auth=SECRET", "refresh failed auth=***"),
                Arguments.of("auth header", "refresh failed Auth: Bearer SECRET", "refresh failed Auth: ***"),
                Arguments.of(
                        "json refresh token",
                        "refresh failed {\"refresh_token\":\"SECRET\"}",
                        "refresh failed {\"refresh_token\":\"***\"}"
                ),
                Arguments.of(
                        "json authorization bearer",
                        "refresh failed {\"authorization\":\"Bearer SECRET\"}",
                        "refresh failed {\"authorization\":\"***\"}"
                ),
                Arguments.of(
                        "spaced verification code assignment",
                        "refresh failed verification code=SECRET",
                        "refresh failed verification code=***"
                ),
                Arguments.of(
                        "spaced verification code header",
                        "refresh failed verification code: SECRET",
                        "refresh failed verification code: ***"
                )
        );
    }

    private static Stream<Arguments> jsonCredentialFieldCases() {
        return Stream.of(
                Arguments.of(
                        "json credential fields",
                        """
                        refresh failed {"access_token":"ACCESS_SECRET","refresh_token":"REFRESH_SECRET","authorization":"Bearer AUTH_SECRET","key":"KEY_SECRET","secret":"CLIENT_SECRET","status":"kept","count":1}
                        """.strip(),
                        """
                        refresh failed {"access_token":"***","refresh_token":"***","authorization":"***","key":"***","secret":"***","status":"kept","count":1}
                        """.strip()
                ),
                Arguments.of(
                        "json credential field with spaces",
                        """
                        refresh failed {"secret":"CLIENT SECRET PLACEHOLDER","status":"kept","count":1}
                        """.strip(),
                        """
                        refresh failed {"secret":"***","status":"kept","count":1}
                        """.strip()
                )
        );
    }
}
