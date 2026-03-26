package checkmo.realtime.web.websocket.controller;

import checkmo.realtime.internal.exception.RealtimeErrorStatus;
import checkmo.realtime.internal.exception.RealtimeException;
import checkmo.realtime.internal.service.PresentationService;
import checkmo.realtime.web.websocket.message.PresentationRequestMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * STOMP를 사용하여 publish하는 메시지를 처리하는 컨트롤러
 */
@Controller
@MessageMapping("/clubs/{clubId}/meetings/{meetingId}/teams/{teamId}")
@RequiredArgsConstructor
public class PresentationController {
    private final PresentationService presentationService;

    @MessageMapping("/presentation")
    public void togglePresentation(
            @DestinationVariable Long clubId, @DestinationVariable Long meetingId, @DestinationVariable Long teamId,
            @Valid PresentationRequestMessage payload, Principal principal
    ) {
        if (principal == null) {
            throw new RealtimeException(RealtimeErrorStatus.UNAUTHENTICATED);
        }
        presentationService.toggleAndPublish(clubId, meetingId, teamId, payload.getTopicId(), payload.getIsSelected());
    }
}
