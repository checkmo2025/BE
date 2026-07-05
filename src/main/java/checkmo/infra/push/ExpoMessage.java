package checkmo.infra.push;

import java.util.Map;

public record ExpoMessage(
        String to,
        String title,
        String body,
        Map<String, Object> data,
        String sound,
        String priority,
        String channelId
) {}
