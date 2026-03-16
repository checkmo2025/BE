package checkmo.realtime.internal.service;

import checkmo.realtime.internal.entity.TeamChatMessage;
import checkmo.realtime.internal.event.RealtimeEvent;
import checkmo.realtime.internal.repository.TeamChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatCommandService {
    private final TeamChatMessageRepository teamChatMessageRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void saveTeamChatMessage(
            Long clubId, Long meetingId, Long teamId,
            String senderMemberId, String content
    ) {
        TeamChatMessage teamChatMessage = teamChatMessageRepository.save(
                TeamChatMessage.builder()
                        .clubId(clubId)
                        .meetingId(meetingId)
                        .teamId(teamId)
                        .senderMemberId(senderMemberId)
                        .content(content)
                        .sentAt(LocalDateTime.now())
                        .build()
        );

        eventPublisher.publishEvent(new RealtimeEvent.TeamChatSavedEvent(clubId, meetingId, teamId, teamChatMessage.getId()));
    }
}
