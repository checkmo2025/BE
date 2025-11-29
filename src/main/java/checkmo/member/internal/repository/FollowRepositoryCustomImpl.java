package checkmo.member.internal.repository;

import static checkmo.member.internal.entity.QFollow.follow;

import checkmo.member.internal.entity.Follow;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FollowRepositoryCustomImpl implements FollowRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Follow> findFollowers(String followingId, Long cursorId, int pageSize) {
        return queryFactory
                .selectFrom(follow)
                .join(follow.follower).fetchJoin()
                .where(
                        follow.following.id.eq(followingId),
                        cursorCondition(cursorId)
                )
                .orderBy(follow.id.desc())
                .limit(pageSize)
                .fetch();
    }

    @Override
    public List<Follow> findFollowings(String followerId, Long cursorId, int pageSize) {
        return queryFactory
                .selectFrom(follow)
                .join(follow.following).fetchJoin()
                .where(
                        follow.follower.id.eq(followerId),
                        cursorCondition(cursorId)
                )
                .orderBy(follow.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression cursorCondition(Long cursorId) {
        return cursorId != null ? follow.id.lt(cursorId) : null;
    }
}