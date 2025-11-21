package checkmo.clubNotice.internal.repository;

import static checkmo.clubNotice.internal.entity.QNotice.notice;

import checkmo.clubNotice.internal.entity.Notice;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class NoticeRepositoryCustomImpl implements NoticeRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notice> findAllByClubIdAndCursorPaging(
            Long clubId,
            boolean onlyImportant,
            Long cursorId,
            Integer size
    ) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(notice.clubId.eq(clubId));
        if (onlyImportant) {
            predicate.and(notice.important.eq(true));
        }
        if (cursorId != null) {
            predicate.and(notice.id.lt(cursorId));
        }

        JPAQuery<Notice> query = queryFactory.selectFrom(notice)
                .where(predicate)
                .orderBy(notice.createdAt.desc());

        if (size != null) {
            query.limit(size);
        }

        return query.fetch();
    }
}
