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
            Long currentMemberId,
            List<MemberInterestCategory> myInterests,
            List<Long> excludedMemberIds,
            int limit
    ) {
        List<Long> followingIds = queryFactory
                .select(follow.following.id)
                .from(follow)
                .where(follow.follower.id.eq(currentMemberId))
                .fetch();

        // 2. 관심사가 없으면 아무나 추천
        if (myInterests == null || myInterests.isEmpty()) {
            return queryFactory
                    .select(member)
                    .from(member)
                    .where(
                            member.id.ne(currentMemberId),
                            notInFollowingIds(followingIds),
                            notInExcludedIds(excludedMemberIds)
                    )
                    .limit(limit)
                    .fetch();
        }

        // 3. 관심사가 있으면 관심사 기반 추천
        EnumPath<MemberInterestCategory> interest =
                Expressions.enumPath(MemberInterestCategory.class, "interest");

        return queryFactory
                .select(member)
                .from(member)
                .join(member.interestCategories, interest)
                .where(
                        member.id.ne(currentMemberId),
                        notInFollowingIds(followingIds),
                        notInExcludedIds(excludedMemberIds),
                        interest.in(myInterests)
                )
                .groupBy(member.id)
                .orderBy(interest.count().desc())
                .limit(limit)
                .fetch();
    }

    private BooleanExpression notInFollowingIds(List<Long> followingIds) {
        return followingIds.isEmpty() ? null : member.id.notIn(followingIds);
    }

    private BooleanExpression notInExcludedIds(List<Long> excludedMemberIds) {
        return excludedMemberIds == null || excludedMemberIds.isEmpty() ? null : member.id.notIn(excludedMemberIds);
    }
}
