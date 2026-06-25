package checkmo.member.web.dto;

import checkmo.member.internal.entity.TermsType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TermsResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "활성 약관 목록 응답")
    public static class PublicTermsList {
        @Schema(description = "현재 활성화된 약관 목록. 서버 표시 순서대로 정렬됩니다.")
        private List<TermsInfo> terms;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "내 약관 동의 상태 응답")
    public static class MemberTermsStatus {
        @Schema(description = "활성 필수 약관 중 아직 동의하지 않은 항목이 있으면 true입니다.", example = "false")
        private boolean requiresRequiredAgreement;

        @Schema(description = "활성 약관별 현재 동의 상태. 서버 표시 순서대로 정렬됩니다.")
        private List<MemberTermsInfo> terms;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "약관 메타데이터")
    public static class TermsInfo {
        @Schema(description = "약관 버전 row ID. 동의 저장 요청의 termsId로 사용합니다.", example = "1")
        private Long id;

        @Schema(description = "약관 종류", example = "SERVICE_TERMS")
        private TermsType termsType;

        @Schema(description = "약관 제목", example = "책모 이용약관 동의")
        private String title;

        @Schema(description = "약관 본문 URL", example = "https://www.checkmo.co.kr/support/terms/service/v1")
        private String termUrl;

        @Schema(description = "약관 버전", example = "1")
        private int version;

        @Schema(description = "필수 동의 여부", example = "true")
        private boolean required;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "약관 메타데이터와 내 최신 동의 상태")
    public static class MemberTermsInfo {
        @Schema(description = "약관 버전 row ID. 동의 저장 요청의 termsId로 사용합니다.", example = "1")
        private Long id;

        @Schema(description = "약관 종류", example = "SERVICE_TERMS")
        private TermsType termsType;

        @Schema(description = "약관 제목", example = "책모 이용약관 동의")
        private String title;

        @Schema(description = "약관 본문 URL", example = "https://www.checkmo.co.kr/support/terms/service/v1")
        private String termUrl;

        @Schema(description = "약관 버전", example = "1")
        private int version;

        @Schema(description = "필수 동의 여부", example = "true")
        private boolean required;

        @Schema(description = "현재 회원의 최신 동의 상태. 이력이 없으면 false입니다.", example = "true")
        private boolean agreed;
    }
}
