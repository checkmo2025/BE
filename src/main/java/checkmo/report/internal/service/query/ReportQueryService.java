package checkmo.report.internal.service.query;

import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.report.internal.converter.ReportConverter;
import checkmo.report.internal.entity.Report;
import checkmo.report.internal.repository.ReportRepository;
import checkmo.report.web.dto.ReportResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ReportRepository reportRepository;

    public ReportResponseDTO.MyReportList retrieveMyReports(String memberId, Long cursorId) {
        CursorResult<Report> reportCursorResult = CursorPagingHelper.getPage(
                size -> reportRepository.findMyReports(memberId, cursorId, size),
                Report::getId,
                DEFAULT_PAGE_SIZE
        );

        List<ReportResponseDTO.ReportInfo> reports = reportCursorResult.content().stream()
                .map(ReportConverter::toReportInfo)
                .toList();

        return ReportResponseDTO.MyReportList.builder()
                .reports(reports)
                .hasNext(reportCursorResult.hasNext())
                .nextCursor(reportCursorResult.nextCursor())
                .build();
    }
}
