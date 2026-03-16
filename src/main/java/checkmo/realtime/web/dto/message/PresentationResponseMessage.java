package checkmo.realtime.web.dto.message;

import lombok.Builder;

@Builder
public record PresentationResponseMessage(
        Long clubId,
        Long meetingId,
        Long teamId,
        Long topicId,
        boolean isSelected
) {
}
