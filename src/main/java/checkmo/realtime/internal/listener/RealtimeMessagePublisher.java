package checkmo.realtime.internal.listener;

import checkmo.realtime.internal.event.RealtimeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RealtimeMessagePublisher {
    private static final String TEAM_CHAT_MESSAGE_DEST_TEMPLATE = "/sub/clubs/%d/meetings/%d/teams/%d/chat/messages";
    private static final String PRESENTATION_TOGGLED_DEST_TEMPLATE = "/sub/clubs/%d/meetings/%d/teams/%d/presentation";

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishChatAfterCommit(RealtimeEvent.TeamChatSavedEvent event) {
        String dest = String.format(TEAM_CHAT_MESSAGE_DEST_TEMPLATE, event.clubId(), event.meetingId(), event.teamId());
        messagingTemplate.convertAndSend(dest, event.message());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishPresentationAfterCommit(RealtimeEvent.PresentationToggledEvent event) {
        String dest = String.format(PRESENTATION_TOGGLED_DEST_TEMPLATE, event.clubId(), event.meetingId(), event.teamId());
        messagingTemplate.convertAndSend(dest, event.message());
    }
}
