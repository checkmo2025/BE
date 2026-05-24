package checkmo.report.internal.repository;

import checkmo.report.internal.entity.Report;

import java.util.List;

public interface ReportRepositoryCustom {

    List<Report> findMyReports(String reporterId, Long cursorId, int pageSize);
}
