package checkmo.realtime.internal.config.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "realtime.websocket")
public record WebSocketProperties(
        List<String> allowedOrigins
) {
}
