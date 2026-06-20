package checkmo.book;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AladinConfigurationTest {

    @Test
    void aladinApiBaseUrlUsesHttps() throws IOException {
        String configuration = Files.readString(Path.of("src/main/resources/application-aladin.yml"));

        assertThat(configuration)
                .contains("base: https://www.aladin.co.kr/ttb/api")
                .doesNotContain("base: http://www.aladin.co.kr/ttb/api");
    }

    @Test
    void recommendationRefreshImmediateRetryDefaultsAreBoundedAndNonSecret() throws IOException {
        String configuration = Files.readString(Path.of("src/main/resources/application-aladin.yml"));

        assertThat(configuration)
                .contains("recommendation:")
                .contains("refresh:")
                .contains("retry:")
                .contains("attempts: 3")
                .contains("initial-backoff: 1s")
                .contains("multiplier: 2")
                .contains("max-backoff: 5s");
    }

    @Test
    void recommendationRefreshBackgroundRetryUsesFiveMinuteFixedDelay() throws IOException {
        String configuration = Files.readString(Path.of("src/main/resources/application-aladin.yml"));

        assertThat(configuration)
                .contains("background:")
                .contains("fixed-delay: 5m");
    }

    @Test
    void recommendationCacheSaveDoesNotUseRedisTtl() throws IOException {
        String service = Files.readString(
                Path.of("src/main/java/checkmo/book/internal/service/BookRecommendationService.java"));

        assertThat(service)
                .contains("book:recommendations:daily")
                .contains("redisTemplate.opsForValue().set(REDIS_KEY, bookList);")
                .contains("redisTemplate.opsForValue().set(REDIS_UPDATED_AT_KEY, LocalDate.now().toString());")
                .doesNotContain("redisTemplate.opsForValue().set(REDIS_KEY, bookList,")
                .doesNotContain("Duration.of");
    }
}
