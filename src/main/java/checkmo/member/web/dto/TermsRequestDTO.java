package checkmo.member.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TermsRequestDTO {

    @Getter
    @NoArgsConstructor
    @Schema(description = "약관 동의 저장 요청")
    public static class SaveAgreements {
        @NotEmpty(message = "약관 동의 항목은 최소 1개 이상이어야 합니다.")
        @Schema(description = "저장할 약관 동의 목록. 최소 1개 이상 필요하며 같은 termsId는 중복 제출할 수 없습니다.")
        private List<@NotNull @Valid Agreement> agreements;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "약관별 동의 상태")
    public static class Agreement {
        @NotNull(message = "약관 ID는 필수입니다.")
        @Schema(description = "활성 약관 ID. GET /api/v1/terms 또는 GET /api/v1/members/me/terms 응답의 id를 사용합니다.", example = "1")
        private Long termsId;

        @NotNull(message = "약관 동의 여부는 필수입니다.")
        @Schema(description = "동의 여부. 필수 약관은 false로 제출할 수 없습니다.", example = "true")
        private Boolean agreed;
    }
}
