package checkmo.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aladin.api")
public class AladinProperties {

    @Valid
    private Url url = new Url();
    @Valid
    private Auth auth = new Auth();
    @Valid
    private Search search = new Search();

    @Getter
    @Setter
    public static class Url {

        @NotBlank(message = "Aladin API 기본 URL은 필수입니다")
        private String base;

        @NotBlank(message = "Item Search URL은 필수입니다")
        private String itemSearch;

        @NotBlank(message = "Item Lookup URL은 필수입니다")
        private String itemLookup;
    }

    @Getter
    @Setter
    public static class Auth {

        @NotBlank(message = "TTB Key는 필수입니다")
        private String ttbKey;

        @NotBlank(message = "API 버전은 필수입니다")
        private String version;
    }

    @Getter
    @Setter
    public static class Search {

        @NotBlank(message = "출력 형식은 필수입니다")
        private String output;

        @Positive(message = "최대 결과 수는 양수여야 합니다")
        @Min(value = 1, message = "최대 결과 수는 1 이상이어야 합니다")
        @Max(value = 100, message = "최대 결과 수는 100 이하여야 합니다")
        private int maxResults;

        @NotBlank(message = "아이템 ID 타입은 필수입니다")
        private String itemIdType;

        @Positive(message = "타임아웃은 양수여야 합니다")
        @Min(value = 1000, message = "타임아웃은 1000ms 이상이어야 합니다")
        private int timeoutMs;
    }
}
