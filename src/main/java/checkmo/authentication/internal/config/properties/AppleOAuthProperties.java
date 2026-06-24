package checkmo.authentication.internal.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.oauth2.apple")
public class AppleOAuthProperties {

    private String teamId;
    private String keyId;
    private String webClientId;
    private String iosClientId;
    private String privateKeyBase64;
    private String webRedirectUri;
}
