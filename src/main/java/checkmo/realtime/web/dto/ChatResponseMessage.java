package checkmo.realtime.web.dto;

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
        String content,
        LocalDateTime sendAt
) {
}
