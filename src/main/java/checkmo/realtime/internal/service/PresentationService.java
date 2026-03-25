package checkmo.realtime.internal.service;

import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.clubMeeting.ClubMeetingExternalDTO;
import checkmo.realtime.internal.exception.RealtimeErrorStatus;
import checkmo.realtime.internal.exception.RealtimeException;
import checkmo.realtime.internal.listener.event.RealtimeEvent;
import checkmo.realtime.web.dto.message.PresentationResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PresentationService {
    private final ClubMeetingAPI clubMeetingAPI;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void toggleAndPublish(Long clubId, Long meetingId, Long teamId, Long topicId, Boolean isSelected) {
        ClubMeetingExternalDTO.ToggleTopicResult result = clubMeetingAPI.toggleTopic(meetingId, teamId, topicId, isSelected);
        if (!result.isSuccess()) {
            throw toRealtimeException(result.failure());
        }
        PresentationResponseMessage message = PresentationResponseMessage.builder()
                .clubId(clubId)
                .meetingId(meetingId)
                .teamId(teamId)
                .topicId(topicId)
                .isSelected(result.isSelected())
                .build();
        eventPublisher.publishEvent(new RealtimeEvent.PresentationToggledEvent(clubId, meetingId, teamId, message));
    }

    private RealtimeException toRealtimeException(ClubMeetingExternalDTO.ToggleTopicResult.Failure failure) {
        return switch (failure) {
            case TEAM_NOT_FOUND -> new RealtimeException(RealtimeErrorStatus.TEAM_NOT_IN_ClUB);
            case TOPIC_NOT_FOUND -> new RealtimeException(RealtimeErrorStatus.TOPIC_NOT_FOUND);
            default -> new RealtimeException(RealtimeErrorStatus.INTERNAL_SERVER_ERROR);
        };
    }
}
