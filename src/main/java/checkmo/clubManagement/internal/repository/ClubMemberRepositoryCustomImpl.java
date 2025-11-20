package checkmo.clubManagement.internal.repository;

import static checkmo.clubManagement.internal.entity.QClubMember.clubMember;

import checkmo.clubManagement.internal.entity.ClubMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ClubMemberRepositoryCustomImpl implements ClubMemberRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ClubMember> findClubMembersByClubIdInClubMemberStatusOrderByIdDesc(
            Long clubId,
            List<ClubMember.ClubMemberStatus> statuses,
            Long cursorId,
            Integer size
    ) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(clubMember.clubId.eq(clubId));

        if (statuses != null && !statuses.isEmpty()) {
            predicate.and(clubMember.clubMemberStatus.in(statuses));
        }
        if (cursorId != null) {
            predicate.and(clubMember.id.lt(cursorId));
        }

        JPAQuery<ClubMember> query = queryFactory
                .selectFrom(clubMember)
                .where(predicate)
                .orderBy(clubMember.id.desc());

        if (size != null) {
            query.limit(size);
        }

        return query.fetch();
    }

    @Override
    public boolean isMemberInClub(String memberId, Long clubId) {
        return queryFactory
                .selectFrom(clubMember)
                .where(clubMember.clubId.eq(clubId)
                        .and(clubMember.memberId.eq(memberId))
                        .and(clubMember.clubMemberStatus.in(
                                ClubMember.ClubMemberStatus.MEMBER,
                                ClubMember.ClubMemberStatus.STAFF
                        )))
                .fetchFirst() != null;
    }

    @Override
    public List<String> findActiveMemberIdsByClubId(Long clubId) {
        return queryFactory
                .select(clubMember.memberId)
                .from(clubMember)
                .where(clubMember.clubId.eq(clubId)
                        .and(clubMember.clubMemberStatus.in(
                                ClubMember.ClubMemberStatus.MEMBER,
                                ClubMember.ClubMemberStatus.STAFF
                        )))
                .fetch();
    }
}
