package checkmo.realtime.web.controller;

import checkmo.realtime.internal.service.ChatService;
import checkmo.realtime.web.dto.ChatRequestMessage;
import checkmo.realtime.web.dto.ChatResponseMessage;
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
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    // 사용자 메시지를 메시지 브로커로 전달(/app)
    // 다음과 같은 메시지는 메시지 브로커로 바로 전달됨 @MessageMapping에 전달 X (/sub, /queue)

    @MessageMapping("/chat/message")
    public void sendMessageToTeamChatRoom(
            @DestinationVariable Long clubId, @DestinationVariable Long meetingId, @DestinationVariable Long teamId,
            @Valid ChatRequestMessage payload, Principal principal
    ) {
        // 메시지 처리 로직 구현 (예: 메시지 브로커로 전달)
        String senderMemberId = (principal != null) ? principal.getName() : "anonymous";
        String content = payload.getContent();
        ChatResponseMessage chatResponseMessage
                = chatService.saveTeamChatMessage(clubId, meetingId, teamId, senderMemberId, content);

        String subscriptionPath
                = String.format("/sub/clubs/%d/meetings/%d/teams/%d/chat/messages", clubId, meetingId, teamId);
        messagingTemplate.convertAndSend(subscriptionPath, chatResponseMessage);
    }
}
