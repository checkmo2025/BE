package checkmo.chatbot.web.controller;

import checkmo.authentication.CurrentId;
import checkmo.chatbot.internal.converter.ChatbotConverter;
import checkmo.chatbot.internal.service.ChatOrchestrationService;
import checkmo.chatbot.web.dto.ChatbotRequestDTO;
import checkmo.chatbot.web.dto.ChatbotResponseDTO;
import checkmo.common.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@Tag(name = "챗봇", description = "책모 사용법 안내 챗봇 API")
public class ChatbotController {

    private final ChatOrchestrationService chatOrchestrationService;

    @Operation(
            summary = "챗봇에게 메시지 전송",
            description = "비로그인 사용자도 이용할 수 있습니다. 처음 대화를 시작할 때는 sessionToken을 비워 보내고, "
                    + "응답으로 받은 sessionToken을 이후 요청에 그대로 실어 보내면 같은 대화로 이어집니다."
    )
    @PostMapping("/messages")
    public ApiResponse<ChatbotResponseDTO.Reply> sendMessage(
            @CurrentId Long memberId,
            @Valid @RequestBody ChatbotRequestDTO.Message request
    ) {
        return ApiResponse.onSuccess(ChatbotConverter.toReplyResponse(
                chatOrchestrationService.respond(request.getSessionToken(), memberId, request.getMessage())
        ));
    }
}
