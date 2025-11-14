package checkmo.notification.web.controller;

import checkmo.common.apiPayload.ApiResponse;
import checkmo.common.CurrentId;
import checkmo.notification.NotificationExternalDTO;
import checkmo.notification.internal.facade.NotificationQueryFacade;
import checkmo.notification.internal.service.command.NotificationCommandService;
import checkmo.notification.web.dto.NotificationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "알림", description = "알림 조회, 읽음 처리, 알림 설정 관련 API")
public class NotificationController {

    private final NotificationQueryFacade notificationQueryFacade;
    private final NotificationCommandService notificationCommandService;

    @Operation(summary = "알림 전체 조회", description = "특정 회원의 전체 알림을 조회합니다.")
    @Parameter(name = "cursorId", description = "커서 ID (페이징을 위한 커서, 처음에는 null)", required = false, example = "10")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    @GetMapping()
    public ApiResponse<NotificationResponseDTO.NotificationListResponse> getNotifications(
            @CurrentId String memberId,
            @RequestParam(required = false) Long cursorId
    ) {
        var notifications = notificationQueryFacade.getNotifications(memberId, cursorId);
        return ApiResponse.onSuccess(notifications);
    }

    @Operation(summary = "읽지 않은 알림 5개 조회", description = "홈화면에서 보여줄 읽지 않은 알림을 조회합니다. Redis 캐시를 사용합니다.")
    @Parameter(name = "size", description = "조회할 알림 개수(필수 아님!!)", required = false, example = "5")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    @GetMapping("/preview")
    public ApiResponse<NotificationExternalDTO.NotificationPreviewList> getUnreadNotifications(
            @CurrentId String memberId,
            @RequestParam(required = false, defaultValue = "5") int size
    ) {
        // Facade를 통해 QueryService의 캐시된 메서드 호출
        var notifications = notificationQueryFacade.getNotificationPreviewList(memberId, size);
        return ApiResponse.onSuccess(notifications);
    }

    @Operation(summary = "알림 읽음 처리", description = "읽음 처리 후 캐시가 무효화되어 다음 조회 시 읽지 않은 알림으로 다시 채워집니다.")
    @Parameter(name = "notificationId", description = "읽음 처리할 알림 ID", required = true, example = "1")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Long> markNotificationAsRead(
            @CurrentId String memberId,
            @PathVariable Long notificationId
    ) {
        notificationCommandService.markNotificationAsRead(notificationId, memberId);
        return ApiResponse.onSuccess(notificationId);
    }
}
