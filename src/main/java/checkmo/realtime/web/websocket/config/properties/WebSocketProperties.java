package checkmo.realtime.web.websocket.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "realtime.websocket")
public record WebSocketProperties(
        List<String> allowedOrigins
) {
}
