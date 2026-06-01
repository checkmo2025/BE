package checkmo.report.internal.converter;


import checkmo.report.internal.entity.Report;
import checkmo.report.web.dto.ReportResponseDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportConverter {

    public static ReportResponseDTO.ReportInfo toReportInfo(
            Report report,
            String displayName,
            String displayImageUrl
    ) {
        return ReportResponseDTO.ReportInfo.builder()
                .reportId(report.getId())
                .targetType(report.getReportTargetType().name())
                .targetTypeDescription(report.getReportTargetType().getDescription())
                .targetId(report.getTargetId())
                .reason(report.getReportReason().name())
                .reasonDescription(report.getReportReason().getDescription())
                .content(report.getContent())
                .redirectUrl(report.getRedirectUrl())
                .displayName(displayName)
                .displayImageUrl(displayImageUrl)
                .reportedAt(report.getCreatedAt())
                .build();
    }

    public static ReportResponseDTO.AdminMemberReportInfo toAdminMemberReportInfo(
            Report report,
            String reporterNickname,
            String reporterProfileImageUrl
    ) {
        return ReportResponseDTO.AdminMemberReportInfo.builder()
                .reportId(report.getId())
                .reportedMemberNickname(reporterNickname)
                .reportedMemberProfileImageUrl(reporterProfileImageUrl)
                .reportType(report.getReportReason().getDescription())
                .content(report.getContent())
                .redirectUrl(report.getRedirectUrl())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
