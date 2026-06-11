package checkmo.common.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SentrySdkCaptureClientTest {

    @Test
    void sanitizeRemovesRawExceptionMessageAndCauseBeforeSending() {
        RuntimeException exception = new RuntimeException(
                "refreshToken=secret password=secret verificationCode=123456"
        );

        Throwable sanitized = SentrySdkCaptureClient.sanitize(exception);

        assertThat(sanitized.getMessage()).isEqualTo("Unexpected backend exception");
        assertThat(sanitized.getCause()).isNull();
        assertThat(sanitized.toString())
                .doesNotContain("refreshToken")
                .doesNotContain("password")
                .doesNotContain("verificationCode")
                .doesNotContain("secret");
        assertThat(sanitized.getStackTrace()).containsExactly(exception.getStackTrace());
    }
}
