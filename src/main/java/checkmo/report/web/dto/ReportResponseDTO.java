package checkmo.report.web.dto;

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