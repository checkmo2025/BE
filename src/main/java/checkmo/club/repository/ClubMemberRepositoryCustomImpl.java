package checkmo.club.repository;

import checkmo.club.entity.ClubMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static checkmo.club.entity.QClubMember.clubMember;

@Repository
@RequiredArgsConstructor
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
}
