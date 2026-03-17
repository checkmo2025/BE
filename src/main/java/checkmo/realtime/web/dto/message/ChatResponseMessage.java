package checkmo.realtime.web.dto.message;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatResponseMessage(
        Long clubId,
        Long meetingId,
        Long teamId,
        String senderMemberId,
        String senderNickname,
        String senderProfileImageUrl,
        Long messageId,
        String content,
        LocalDateTime sendAt
) {
}
