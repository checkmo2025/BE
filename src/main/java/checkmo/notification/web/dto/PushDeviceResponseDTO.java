package checkmo.notification.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "푸시 디바이스 등록·갱신 응답")
public class PushDeviceResponseDTO {

    @Schema(description = "디바이스 ID", example = "142")
    private Long deviceId;

    @Schema(description = "설치 UUID (서버 생성 시에도 반환)", example = "70cb28d6-1118-4c26-bf14-a73edfde97df")
    private String installationId;

    @Schema(description = "활성 여부", example = "true")
    private boolean active;

    @Schema(description = "등록 일시", example = "2026-06-22T12:00:00")
    private LocalDateTime registeredAt;
}
