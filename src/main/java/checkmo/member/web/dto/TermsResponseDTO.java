package checkmo.member.web.dto;

import checkmo.member.internal.entity.TermsType;
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
    public static class PublicTermsList {
        private List<TermsInfo> terms;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberTermsStatus {
        private boolean requiresRequiredAgreement;
        private List<MemberTermsInfo> terms;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TermsInfo {
        private Long id;
        private TermsType termsType;
        private String title;
        private String termUrl;
        private int version;
        private boolean required;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberTermsInfo {
        private Long id;
        private TermsType termsType;
        private String title;
        private String termUrl;
        private int version;
        private boolean required;
        private boolean agreed;
    }
}
