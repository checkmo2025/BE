package checkmo.member.internal.repository;

import static checkmo.member.internal.entity.QMemberReport.memberReport;

import checkmo.member.internal.entity.MemberReport;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberReportRepositoryCustomImpl implements MemberReportRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberReport> findMyReports(String reporterId, Long cursorId, int pageSize) {
        return queryFactory
                .selectFrom(memberReport)
                .where(
                        memberReport.reporter.id.eq(reporterId),
                        cursorCondition(cursorId)
                )
                .orderBy(memberReport.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression cursorCondition(Long cursorId) {
        return cursorId != null ? memberReport.id.lt(cursorId) : null;
    }
}
