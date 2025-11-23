package checkmo.clubManagement.internal.repository;

import static checkmo.clubManagement.internal.entity.QBookRecommend.bookRecommend;

import checkmo.clubManagement.internal.entity.BookRecommend;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class BookRecommendRepositoryCustomImpl implements BookRecommendRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<BookRecommend> getBookRecommendsAndClubMemberByClubIdAndCursor(
            Long clubId,
            Long cursorId,
            Integer size
    ) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(bookRecommend.clubMember.club.id.eq(clubId));

        if (cursorId != null) {
            predicate.and(bookRecommend.id.lt(cursorId));
        }

        JPAQuery<BookRecommend> query = queryFactory.selectFrom(bookRecommend)
                .join(bookRecommend.clubMember)
                .fetchJoin()
                .where(predicate)
                .orderBy(bookRecommend.id.desc());

        if (size != null) {
            query.limit(size);
        }

        return query.fetch();
    }
}
