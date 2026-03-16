package checkmo.realtime.internal.event;

import checkmo.realtime.web.dto.ChatResponseMessage;
import checkmo.realtime.web.dto.PresentationResponseMessage;
import lombok.Builder;

public class RealtimeEvent {
    @Builder
    public record TeamChatSavedEvent(Long clubId, Long meetingId, Long teamId, ChatResponseMessage message) {
    }

    @Builder
    public record PresentationToggledEvent(
            Long clubId, Long meetingId, Long teamId,
            PresentationResponseMessage message
    ) {
    }
}
