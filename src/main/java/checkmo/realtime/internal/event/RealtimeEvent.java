package checkmo.realtime.internal.event;

import checkmo.realtime.web.dto.message.PresentationResponseMessage;
import lombok.Builder;

public class RealtimeEvent {
    @Builder
    public record TeamChatSavedEvent(Long clubId, Long meetingId, Long teamId, Long meessageId) {
    }

    @Builder
    public record PresentationToggledEvent(
            Long clubId, Long meetingId, Long teamId,
            PresentationResponseMessage message
    ) {
    }
}
