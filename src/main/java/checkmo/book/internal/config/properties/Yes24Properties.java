package checkmo.book.internal.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "yes24.api")
public class Yes24Properties {

    private String baseUrl = "https://apis.yes24.com/v1";
    private String apiKey;
    @Valid
    private Search search = new Search();
    @Valid
    private Recommendation recommendation = new Recommendation();

    @Getter
    @Setter
    public static class Search {
        @Positive(message = "최대 결과 수는 양수여야 합니다")
        @Min(value = 1, message = "최대 결과 수는 1 이상이어야 합니다")
        @Max(value = 100, message = "최대 결과 수는 100 이하여야 합니다")
        private int maxResults = 10;

        @Positive(message = "타임아웃은 양수여야 합니다")
        @Min(value = 1000, message = "타임아웃은 1000ms 이상이어야 합니다")
        private int timeoutMs = 15000;
    }

    @Getter
    @Setter
    public static class Recommendation {
        @Valid
        private Refresh refresh = new Refresh();
    }

    @Getter
    @Setter
    public static class Refresh {
        @Valid
        private Retry retry = new Retry();
        @Valid
        private Background background = new Background();
    }

    @Getter
    @Setter
    public static class Retry {
        @Min(value = 1, message = "추천 책 갱신 재시도 횟수는 1 이상이어야 합니다")
        @Max(value = 10, message = "추천 책 갱신 재시도 횟수는 10 이하여야 합니다")
        private int attempts = 3;

        @DurationMin(millis = 1, message = "추천 책 갱신 초기 backoff는 양수여야 합니다")
        private Duration initialBackoff = Duration.ofSeconds(1);

        @DecimalMin(value = "1.0", inclusive = true, message = "추천 책 갱신 backoff 배수는 1 이상이어야 합니다")
        private double multiplier = 2;

        @DurationMin(millis = 1, message = "추천 책 갱신 최대 backoff는 양수여야 합니다")
        private Duration maxBackoff = Duration.ofSeconds(5);
    }

    @Getter
    @Setter
    public static class Background {
        @DurationMin(millis = 1, message = "추천 책 갱신 background fixed delay는 양수여야 합니다")
        private Duration fixedDelay = Duration.ofMinutes(5);
    }
}
