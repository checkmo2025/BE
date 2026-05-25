package checkmo.report.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ReportResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportInfo {
        private Long reportId;

        private String targetType;
        private String targetTypeDescription;
        private String targetId;
        private String targetSummary;

        private String reason;
        private String reasonDescription;

        private String content;
        private String redirectUrl;

        @Schema(description = "독서모임과 독서모임 공지사항은 독서모임 이름 + 독서모임 이미지 제공, 이외는 신고 대상의 작성자 이름 + 작성자 이미지 제공")
        private String displayName;
        private String displayImageUrl;

        private LocalDateTime reportedAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyReportList {
        private List<ReportInfo> reports;
        private boolean hasNext;
        private Long nextCursor;
    }

}