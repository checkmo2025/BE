package checkmo.realtime.web.controller;

import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.realtime.web.dto.PresentationRequestMessage;
import checkmo.realtime.web.dto.PresentationResponseMessage;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * STOMP를 사용하여 publish하는 메시지를 처리하는 컨트롤러
 */
@Controller
@MessageMapping("/clubs/{clubId}/meetings/{meetingId}/teams/{teamId}")
@RequiredArgsConstructor
public class PresentationController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ClubMeetingAPI clubMeetingAPI;

    @MessageMapping("/presentation")
    public void togglePresentation(
            @DestinationVariable Long clubId, @DestinationVariable Long meetingId, @DestinationVariable Long teamId,
            @Valid PresentationRequestMessage payload, Principal principal
    ) {
        String memberId = principal.getName();
        Long topicId = payload.getTopicId();
        Boolean isSelected = payload.getIsSelected();
        boolean result = clubMeetingAPI.toggleTopic(clubId, meetingId, teamId, topicId, isSelected, memberId);

        PresentationResponseMessage presentationResponseMessage = PresentationResponseMessage.builder()
                .clubId(clubId)
                .meetingId(meetingId)
                .teamId(teamId)
                .topicId(topicId)
                .isSelected(result)
                .build();

        String subscriptionPath
                = String.format("/sub/clubs/%d/meetings/%d/teams/%d/presentation", clubId, meetingId, teamId);
        messagingTemplate.convertAndSend(subscriptionPath, presentationResponseMessage);
    }
}
