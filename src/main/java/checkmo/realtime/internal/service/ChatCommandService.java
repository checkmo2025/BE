package checkmo.realtime.internal.service;

import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.realtime.internal.entity.TeamChatMessage;
import checkmo.realtime.internal.exception.RealtimeErrorStatus;
import checkmo.realtime.internal.exception.RealtimeException;
import checkmo.realtime.internal.listener.event.RealtimeEvent;
import checkmo.realtime.internal.repository.TeamChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatCommandService {
    private final ClubMeetingAPI clubMeetingAPI;
    private final TeamChatMessageRepository teamChatMessageRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void saveTeamChatMessage(
            Long clubId, Long meetingId, Long teamId,
            Long senderMemberId, String content
    ) {
        if (clubMeetingAPI.isChatDisabled(meetingId)) {
            throw new RealtimeException(RealtimeErrorStatus.CHAT_DISABLED);
        }
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
