package checkmo.infra;

import java.util.Map;

public record PushSendRequest(
        String token,
        String title,
        String body,
        Map<String, Object> data,
        String sound,
        String priority,
        String channelId
) {}
