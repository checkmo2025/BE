package checkmo.notification.web.controller;

import checkmo.authentication.CurrentId;
import checkmo.common.apiPayload.ApiResponse;
import checkmo.notification.internal.converter.PushDeviceConverter;
import checkmo.notification.internal.service.command.PushDeviceCommandService;
import checkmo.notification.web.dto.NotificationSettingType;
import checkmo.notification.internal.service.NotificationQueryFacade;
import checkmo.notification.internal.service.command.NotificationCommandService;
import checkmo.notification.internal.service.command.NotificationSettingCommandService;
import checkmo.notification.web.dto.NotificationResponseDTO.BasicInfoList;
import checkmo.notification.web.dto.NotificationResponseDTO.BasicInfoPreviewList;
import checkmo.notification.web.dto.NotificationResponseDTO.SettingInfo;
import checkmo.notification.web.dto.PushDeviceRequestDTO;
import checkmo.notification.web.dto.PushDeviceResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Tag(name = "알림", description = "알림 조회, 읽음 처리, 알림 설정 관련 API")
@RequestMapping("/api/v1/notifications")
@RestController
public class NotificationController {

    private final NotificationQueryFacade notificationQueryFacade;
    private final NotificationCommandService notificationCommandService;
    private final NotificationSettingCommandService notificationSettingCommandService;
    private final PushDeviceCommandService pushDeviceCommandService;

    @Operation(summary = "푸시 디바이스 등록·갱신",
            description = "installationId 기준으로 upsert합니다. 같은 token이 다른 설치에 있으면 이전 설치를 비활성화합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 또는 갱신 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다.")
    })
    @PutMapping("/push-devices")
    public ApiResponse<PushDeviceResponseDTO> registerPushDevice(
            @CurrentId String memberId,
            @RequestBody @Valid PushDeviceRequestDTO request
    ) {
        var device = pushDeviceCommandService.upsert(memberId, request);
        return ApiResponse.onSuccess(PushDeviceConverter.toResponse(device));
    }

    @Operation(summary = "푸시 디바이스 해제",
            description = "해당 installationId의 디바이스를 비활성화합니다. 존재하지 않거나 이미 비활성인 경우에도 성공을 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다.")
    })
    @DeleteMapping("/push-devices/{installationId}")
    public ApiResponse<Void> deregisterPushDevice(
            @CurrentId String memberId,
            @PathVariable String installationId
    ) {
        pushDeviceCommandService.deactivate(memberId, installationId);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "알림 전체 조회", description = "특정 회원의 전체 알림을 조회합니다.")
    @Parameter(name = "cursorId", description = "커서 ID (페이징을 위한 커서, 처음에는 null)", required = false, example = "10")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    @GetMapping()
    public ApiResponse<BasicInfoList> getNotifications(
            @CurrentId String memberId,
            @RequestParam(required = false) Long cursorId
    ) {
        var notifications = notificationQueryFacade.retrieveNotifications(memberId, cursorId);
        return ApiResponse.onSuccess(notifications);
    }

    @Operation(summary = "읽지 않은 알림 5개 조회", description = "홈화면에서 보여줄 읽지 않은 알림을 조회합니다. Redis 캐시를 사용합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    @GetMapping("/preview")
    public ApiResponse<BasicInfoPreviewList> getUnreadNotifications(
            @CurrentId String memberId
    ) {
        // Facade를 통해 QueryService의 캐시된 메서드 호출
        var notifications = notificationQueryFacade.retrieveNotificationPreviews(memberId);
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

    @Operation(summary = "알림 설정 조회", description = "회원의 알림 설정을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "알림 설정을 찾을 수 없음")
    })
    @GetMapping("/settings")
    public ApiResponse<SettingInfo> getNotificationSetting(
            @CurrentId String memberId
    ) {
        var setting = notificationQueryFacade.retrieveNotificationSetting(memberId);
        return ApiResponse.onSuccess(setting);
    }

    @Operation(summary = "알림 설정 토글", description = "특정 알림 설정을 토글합니다. (켜짐 <-> 꺼짐)")
    @Parameter(name = "settingType", description = "알림 설정 타입", required = true, example = "BOOK_STORY_LIKED")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요한 서비스 입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "알림 설정을 찾을 수 없음")
    })
    @PatchMapping("/settings/{settingType}")
    public ApiResponse<Void> toggleNotificationSetting(
            @CurrentId String memberId,
            @PathVariable NotificationSettingType settingType
    ) {
        notificationSettingCommandService.toggleNotificationSetting(memberId, settingType);
        return ApiResponse.onSuccess(null);
    }
}
