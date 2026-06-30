package checkmo.appVersion.web.controller;

import checkmo.appVersion.internal.converter.AppVersionConverter;
import checkmo.appVersion.internal.service.query.AppVersionQueryService;
import checkmo.appVersion.web.dto.AppVersionResponseDTO.VersionPolicy;
import checkmo.common.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
@Tag(name = "앱 버전", description = "앱 버전 정책 조회 API")
public class AppVersionController {

    private final AppVersionQueryService appVersionQueryService;

    @Operation(
            summary = "앱 버전 정책 조회",
            description = "iOS/Android 앱이 부팅 또는 로그인 시 호출하는 버전 정책을 조회합니다. 버전 비교는 앱에서 수행합니다."
    )
    @GetMapping("/version")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "platform은 ios 또는 android만 가능합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "앱 버전 정책을 찾을 수 없습니다.")
    })
    public ApiResponse<VersionPolicy> getVersionPolicy(
            @Parameter(description = "앱 플랫폼", example = "ios")
            @RequestParam String platform
    ) {
        return ApiResponse.onSuccess(AppVersionConverter.toVersionPolicy(
                appVersionQueryService.retrieveActivePolicy(platform)
        ));
    }
}
