package checkmo.realtime.web.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ChatResponseMessage(
        Long clubId,
        Long meetingId,
        Long teamId,
        String senderMemberId,
        String senderNickname,
        String senderProfileImageUrl,
        String content,
        LocalDateTime sendAt
) {
}
