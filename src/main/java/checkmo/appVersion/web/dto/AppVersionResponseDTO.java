package checkmo.appVersion.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AppVersionResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "앱 버전 정책 응답")
    public static class VersionPolicy {
        @Schema(description = "강제 업데이트가 필요한 최소 지원 버전", example = "1.0.2")
        private String minSupportedVersion;

        @Schema(description = "스토어에 배포된 최신 권장 버전", example = "1.1.0")
        private String latestVersion;

        @Schema(description = "플랫폼별 스토어 이동 URL", example = "https://apps.apple.com/app/id000000000")
        private String storeUrl;
    }
}
