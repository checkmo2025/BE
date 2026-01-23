package checkmo.member.internal.repository;

import static checkmo.member.internal.entity.QFollow.follow;
import static checkmo.member.internal.entity.QMember.member;

import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.MemberInterestCategory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Member> findRecommendMembers(
            String currentMemberId,
            List<MemberInterestCategory> myInterests,
            int limit
    ) {
        EnumPath<MemberInterestCategory> interest =
                Expressions.enumPath(MemberInterestCategory.class, "interest");

        // 1. 이미 팔로우한 사람들의 ID 조회 (제외 대상)
        List<String> followingIds = queryFactory
                .select(follow.following.id)
                .from(follow)
                .where(follow.follower.id.eq(currentMemberId))
                .fetch();

        // 2. 추천 쿼리 실행
        return queryFactory
                .select(member)
                .from(member)
                .join(member.interestCategories, interest)
                .where(
                        member.id.ne(currentMemberId),
                        notInFollowingIds(followingIds),
                        interest.in(myInterests)
                )
                .groupBy(member.id)
                .orderBy(interest.count().desc())
                .limit(limit)
                .fetch();
    }

    private BooleanExpression notInFollowingIds(List<String> followingIds) {
        return followingIds.isEmpty() ? null : member.id.notIn(followingIds);
    }
}
