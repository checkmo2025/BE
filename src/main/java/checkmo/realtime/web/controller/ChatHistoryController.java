package checkmo.realtime.web.controller;

import checkmo.authentication.CurrentId;
import checkmo.common.apiPayload.ApiResponse;
import checkmo.realtime.internal.service.AuthorizationService;
import checkmo.realtime.internal.service.ChatQueryService;
import checkmo.realtime.web.dto.ChatResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Tag(name = "채팅", description = "채팅 내역 조회 API")
@RequestMapping("/api/clubs/{clubId}/meetings/{meetingId}/teams/{teamId}/chat")
@RestController
public class ChatHistoryController {
    private final ChatQueryService chatQueryService;
    private final AuthorizationService authorizationService;

    @Operation(summary = "채팅 내역 조회", description = "cursorId로부터 최근 30개의 채팅 메시지를 조회합니다.(cursorId가 없는 경우 가장 최근 30개의 메시지를 조회)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @GetMapping("/messages")
    public ApiResponse<ChatResponseDTO.ChatHistoryList> getMessages(
            @PathVariable Long clubId,
            @PathVariable Long meetingId,
            @PathVariable Long teamId,
            @RequestParam(required = false) Long cursorId,
            @CurrentId String memberId
    ) {
        authorizationService.authorizeTeamAccess(clubId, meetingId, teamId, memberId);
        return ApiResponse.onSuccess(chatQueryService.fetchHistory(clubId, meetingId, teamId, cursorId));
    }
}
