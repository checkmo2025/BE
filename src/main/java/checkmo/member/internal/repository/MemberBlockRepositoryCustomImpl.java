package checkmo.member.internal.repository;

import static checkmo.member.internal.entity.QMemberBlock.memberBlock;

import checkmo.member.internal.entity.MemberBlock;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberBlockRepositoryCustomImpl implements MemberBlockRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberBlock> findBlocks(Long blockerId, Long cursorId, int pageSize) {
        return queryFactory
                .selectFrom(memberBlock)
                .join(memberBlock.blocked).fetchJoin()
                .where(
                        memberBlock.blocker.id.eq(blockerId),
                        cursorCondition(cursorId)
                )
                .orderBy(memberBlock.id.desc())
                .limit(pageSize)
                .fetch();
    }

    @Override
    public List<Long> findBlockedMemberIds(Long blockerId) {
        return queryFactory
                .select(memberBlock.blocked.id)
                .from(memberBlock)
                .where(memberBlock.blocker.id.eq(blockerId))
                .fetch();
    }

    @Override
    public List<Long> findBlockRelatedMemberIds(Long memberId) {
        return queryFactory
                .select(new CaseBuilder()
                        .when(memberBlock.blocker.id.eq(memberId))
                        .then(memberBlock.blocked.id)
                        .otherwise(memberBlock.blocker.id))
                .from(memberBlock)
                .where(memberBlock.blocker.id.eq(memberId)
                        .or(memberBlock.blocked.id.eq(memberId)))
                .distinct()
                .fetch();
    }

    @Override
    public Optional<MemberBlock> findBetween(Long memberId1, Long memberId2) {
        return Optional.ofNullable(queryFactory
                .selectFrom(memberBlock)
                .where(between(memberId1, memberId2))
                .fetchFirst());
    }

    @Override
    public boolean existsBetween(Long memberId1, Long memberId2) {
        Integer result = queryFactory
                .selectOne()
                .from(memberBlock)
                .where(between(memberId1, memberId2))
                .fetchFirst();

        return result != null;
    }

    private BooleanExpression between(Long memberId1, Long memberId2) {
        return memberBlock.blocker.id.eq(memberId1).and(memberBlock.blocked.id.eq(memberId2))
                .or(memberBlock.blocker.id.eq(memberId2).and(memberBlock.blocked.id.eq(memberId1)));
    }

    private BooleanExpression cursorCondition(Long cursorId) {
        return cursorId != null ? memberBlock.id.lt(cursorId) : null;
    }
}
