package checkmo.realtime.internal.service;

import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.realtime.internal.event.RealtimeEvent;
import checkmo.realtime.web.dto.PresentationResponseMessage;
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
    public void toggleAndPublish(Long clubId, Long meetingId, Long teamId, Long topicId, Boolean isSelected, String memberId) {
        boolean result = clubMeetingAPI.toggleTopic(clubId, meetingId, teamId, topicId, isSelected, memberId);
        PresentationResponseMessage message = PresentationResponseMessage.builder()
                .clubId(clubId)
                .meetingId(meetingId)
                .teamId(teamId)
                .topicId(topicId)
                .isSelected(result)
                .build();
        eventPublisher.publishEvent(new RealtimeEvent.PresentationToggledEvent(clubId, meetingId, teamId, message));
    }
}
