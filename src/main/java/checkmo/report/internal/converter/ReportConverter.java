package checkmo.report.internal.converter;


import checkmo.report.internal.entity.Report;
import checkmo.report.web.dto.ReportResponseDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportConverter {

    public static ReportResponseDTO.ReportInfo toReportInfo(Report report) {
        return ReportResponseDTO.ReportInfo.builder()
                .reportId(report.getId())
                .targetType(report.getReportTargetType().name())
                .targetTypeDescription(report.getReportTargetType().getDescription())
                .targetId(report.getTargetId())
                .reason(report.getReportReason().name())
                .reasonDescription(report.getReportReason().getDescription())
                .content(report.getContent())
                .redirectUrl(report.getRedirectUrl())
                .reportedAt(report.getCreatedAt())
                .build();
    }
}