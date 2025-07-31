package checkmo.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private TokenValidity tokenValidity;

    @Getter
    @Setter
    public static class TokenValidity {
        private Long accessToken;
        private Long refreshToken;
    }
}
