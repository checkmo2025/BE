package checkmo.infra.push.internal.config.properties;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "expo.push")
public class ExpoPushProperties {

    private String baseUrl = "https://exp.host/--/api/v2";

    private String accessToken;

    private boolean enabled;

    @Positive
    private int connectTimeout = 5000;

    @Positive
    private int readTimeout = 10000;
}
