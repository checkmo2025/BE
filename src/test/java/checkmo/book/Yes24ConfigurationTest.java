package checkmo.book;

import static org.assertj.core.api.Assertions.assertThat;

import checkmo.book.internal.config.properties.Yes24Properties;
import jakarta.validation.Validation;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class Yes24ConfigurationTest {

    @Test
    void yes24ApiBaseUrlUsesHttps() throws IOException {
        Map<String, Object> api = yes24ApiConfiguration();

        assertThat(api.get("base-url"))
                .isEqualTo("https://apis.yes24.com/v1");
    }

    @Test
    void recommendationRefreshImmediateRetryDefaultsAreBoundedAndNonSecret() throws IOException {
        Map<String, Object> retry = recommendationRetryConfiguration();

        assertThat(retry)
                .containsEntry("attempts", 3)
                .containsEntry("initial-backoff", "1s")
                .containsEntry("multiplier", 2)
                .containsEntry("max-backoff", "5s");
    }

    @Test
    void recommendationRefreshBackgroundRetryUsesFiveMinuteFixedDelay() throws IOException {
        Map<String, Object> api = yes24ApiConfiguration();
        Map<String, Object> recommendation = nestedMap(api, "recommendation");
        Map<String, Object> refresh = nestedMap(recommendation, "refresh");
        Map<String, Object> background = nestedMap(refresh, "background");

        assertThat(background)
                .containsEntry("fixed-delay", "5m");
    }

    @Test
    void recommendationRefreshRetryAttemptsRejectsValuesAboveTen() {
        var retry = new Yes24Properties.Retry();
        retry.setAttempts(11);

        var violations = Validation.buildDefaultValidatorFactory()
                .getValidator()
                .validate(retry);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("attempts");
                    assertThat(violation.getMessage()).isEqualTo("추천 책 갱신 재시도 횟수는 10 이하여야 합니다");
                });
    }

    @Test
    void recommendationRefreshRetryMultiplierRejectsValuesBelowOne() {
        var retry = new Yes24Properties.Retry();
        retry.setMultiplier(0.5);

        var violations = Validation.buildDefaultValidatorFactory()
                .getValidator()
                .validate(retry);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("multiplier");
                    assertThat(violation.getMessage()).isEqualTo("추천 책 갱신 backoff 배수는 1 이상이어야 합니다");
                });
    }

    private Map<String, Object> recommendationRetryConfiguration() throws IOException {
        Map<String, Object> api = yes24ApiConfiguration();
        Map<String, Object> recommendation = nestedMap(api, "recommendation");
        Map<String, Object> refresh = nestedMap(recommendation, "refresh");
        return nestedMap(refresh, "retry");
    }

    private Map<String, Object> yes24ApiConfiguration() throws IOException {
        try (var input = getClass().getClassLoader().getResourceAsStream("application-yes24.yml")) {
            assertThat(input).isNotNull();
            Map<String, Object> configuration = new Yaml().load(input);
            Map<String, Object> yes24 = nestedMap(configuration, "yes24");
            return nestedMap(yes24, "api");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nestedMap(Map<String, Object> source, String key) {
        return (Map<String, Object>) source.get(key);
    }
}
