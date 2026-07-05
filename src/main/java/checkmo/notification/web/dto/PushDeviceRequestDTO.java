package checkmo.notification.web.dto;

import checkmo.notification.internal.entity.PushDevice.PushPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "푸시 디바이스 등록·갱신 요청")
public class PushDeviceRequestDTO {

    @Size(max = 36)
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "UUID 형식이어야 합니다.")
    @Schema(description = "앱 설치 단위 UUID. null이면 서버가 생성한다.", example = "70cb28d6-1118-4c26-bf14-a73edfde97df", nullable = true)
    private String installationId;

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^(ExponentPushToken|ExpoPushToken)\\[.+\\]$",
            message = "ExponentPushToken[...] 또는 ExpoPushToken[...] 형식이어야 합니다.")
    @Schema(description = "Expo Push Token", example = "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]")
    private String expoPushToken;

    @NotNull
    @Schema(description = "플랫폼", example = "IOS")
    private PushPlatform platform;

    @NotBlank
    @Size(max = 32)
    @Schema(description = "앱 버전", example = "1.0.1")
    private String appVersion;

    @NotBlank
    @Size(max = 32)
    @Schema(description = "빌드 번호", example = "12")
    private String buildNumber;
}
