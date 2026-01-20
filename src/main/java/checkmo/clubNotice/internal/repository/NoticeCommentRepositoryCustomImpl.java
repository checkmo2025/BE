package checkmo.clubNotice.internal.repository;

import static checkmo.clubNotice.internal.entity.QNoticeComment.noticeComment;

import checkmo.clubNotice.internal.entity.NoticeComment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class NoticeCommentRepositoryCustomImpl implements NoticeCommentRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<NoticeComment> findAllByNoticeIdAndCursorPaging(Long noticeId, Long cursorId, Integer size) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(noticeComment.notice.id.eq(noticeId));

        if (cursorId != null) {
            predicate.and(noticeComment.id.lt(cursorId));
        }

        JPAQuery<NoticeComment> query = queryFactory.selectFrom(noticeComment)
                .where(predicate)
                .orderBy(noticeComment.createdAt.desc());
        
        if (size != null) {
            query.limit(size);
        }

        return query.fetch();
    }
}
