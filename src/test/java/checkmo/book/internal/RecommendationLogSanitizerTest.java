package checkmo.book.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RecommendationLogSanitizerTest {

    private static final String SECRET = "SECRET";

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
}
