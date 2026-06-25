package checkmo.member.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TermsRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class SaveAgreements {
        @NotEmpty(message = "약관 동의 항목은 최소 1개 이상이어야 합니다.")
        private List<@NotNull @Valid Agreement> agreements;
    }

    @Getter
    @NoArgsConstructor
    public static class Agreement {
        @NotNull(message = "약관 ID는 필수입니다.")
        private Long termsId;

        @NotNull(message = "약관 동의 여부는 필수입니다.")
        private Boolean agreed;
    }
}
