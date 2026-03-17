package checkmo.realtime.internal.listener;

import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import checkmo.realtime.internal.entity.TeamChatMessage;
import checkmo.realtime.internal.event.RealtimeEvent;
import checkmo.realtime.internal.exception.RealtimeErrorStatus;
import checkmo.realtime.internal.exception.RealtimeException;
import checkmo.realtime.internal.repository.TeamChatMessageRepository;
import checkmo.realtime.web.dto.message.ChatResponseMessage;
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

    private final MemberAPI memberAPI;

    private final TeamChatMessageRepository teamChatMessageRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishChatAfterCommit(RealtimeEvent.TeamChatSavedEvent event) {
        TeamChatMessage teamChatMessage = teamChatMessageRepository.findById(event.meessageId())
                .orElseThrow(() -> new RealtimeException(RealtimeErrorStatus.MESSAGE_NOT_FOUND));
        MemberExternalDTO.BasicInfo basicInfo = memberAPI.fetchMemberBasicInfo(teamChatMessage.getSenderMemberId());
        String dest = String.format(TEAM_CHAT_MESSAGE_DEST_TEMPLATE, event.clubId(), event.meetingId(), event.teamId());
        ChatResponseMessage message = ChatResponseMessage.builder()
                .clubId(event.clubId())
                .meetingId(event.meetingId())
                .teamId(event.teamId())
                .senderMemberId(teamChatMessage.getSenderMemberId())
                .senderNickname(basicInfo.getNickname())
                .senderProfileImageUrl(basicInfo.getProfileImageUrl())
                .messageId(teamChatMessage.getId())
                .content(teamChatMessage.getContent())
                .sendAt(teamChatMessage.getSentAt())
                .build();
        messagingTemplate.convertAndSend(dest, message);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishPresentationAfterCommit(RealtimeEvent.PresentationToggledEvent event) {
        String dest = String.format(PRESENTATION_TOGGLED_DEST_TEMPLATE, event.clubId(), event.meetingId(), event.teamId());
        messagingTemplate.convertAndSend(dest, event.message());
    }
}
