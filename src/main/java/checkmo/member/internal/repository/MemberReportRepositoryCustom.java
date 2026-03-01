package checkmo.member.internal.repository;

import checkmo.member.internal.entity.MemberReport;
import java.util.List;

public interface MemberReportRepositoryCustom {

    List<MemberReport> findMyReports(String reporterId, Long cursorId, int pageSize);
}
