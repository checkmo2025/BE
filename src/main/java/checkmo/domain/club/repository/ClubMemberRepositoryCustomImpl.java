package checkmo.domain.club.repository;

import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.entity.QClubMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ClubMemberRepositoryCustomImpl implements ClubMemberRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QClubMember clubMember = QClubMember.clubMember;

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
