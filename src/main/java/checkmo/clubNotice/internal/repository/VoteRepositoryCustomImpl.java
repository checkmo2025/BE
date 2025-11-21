package checkmo.clubNotice.internal.repository;

import static checkmo.clubNotice.internal.entity.QVote.vote;

import checkmo.clubNotice.internal.entity.Vote;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class VoteRepositoryCustomImpl implements VoteRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Vote> findByClubIdAndCursorPaging(Long clubId, boolean onlyImportant, Long cursorId, Integer size) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(vote.clubId.eq(clubId));
        if (onlyImportant) {
            predicate.and(vote.important.eq(true));
        }
        if (cursorId != null) {
            predicate.and(vote.id.lt(cursorId));
        }

        JPAQuery<Vote> query = queryFactory.selectFrom(vote)
                .where(predicate)
                .orderBy(vote.createdAt.desc());

        if (size != null) {
            query.limit(size);
        }

        return query.fetch();
    }
}
