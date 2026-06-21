package checkmo.report.internal.repository;

import checkmo.report.internal.entity.Report;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static checkmo.report.internal.entity.QReport.report;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryCustomImpl implements ReportRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Report> findMyReports(String reporterId, Long cursorId, int pageSize) {
        return queryFactory
                .selectFrom(report)
                .where(
                        report.reporterId.eq(reporterId),
                        cursorCondition(cursorId)
                )
                .orderBy(report.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression cursorCondition(Long cursorId) {
        return cursorId != null ? report.id.lt(cursorId) : null;
    }
}
