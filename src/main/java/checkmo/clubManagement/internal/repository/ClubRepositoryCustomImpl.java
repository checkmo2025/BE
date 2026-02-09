package checkmo.clubManagement.internal.repository;

import static checkmo.clubManagement.internal.entity.QClub.club;
import static checkmo.clubManagement.internal.entity.QClubMember.clubMember;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubInterestCategory;
import checkmo.clubManagement.internal.entity.ClubMemberStatus;
import checkmo.clubManagement.internal.entity.ClubParticipantType;
import checkmo.clubManagement.internal.entity.QClubMember;
import checkmo.clubManagement.internal.repository.projection.ClubRecommendation;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubRequestDTO.ClubSearchOutputFilter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClubRepositoryCustomImpl implements ClubRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Club> searchClubs(ClubRequestDTO.ClubSearchFilter filter, Long cursorId, Integer size) {
        BooleanBuilder builder = buildSearchCondition(filter);

        if (cursorId != null) {
            builder.and(club.id.lt(cursorId));
        }

        return queryFactory
                .selectFrom(club)
                .where(builder)
                .orderBy(club.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanBuilder buildSearchCondition(ClubRequestDTO.ClubSearchFilter filter) {
        BooleanBuilder builder = new BooleanBuilder();

        String keyword = filter.keyword(); // 이미 trim + null은 ""로 처리됨

        if (!keyword.isBlank()) {
            switch (filter.inputFilter()) {
                case NAME -> builder.and(club.name.containsIgnoreCase(keyword));
                case REGION -> builder.and(club.region.containsIgnoreCase(keyword));
            }
        }

        ClubSearchOutputFilter outputFilter = filter.outputFilter();
        ClubParticipantType participantType = outputFilter.toClubParticipantTypeOrNull();
        if (participantType != null) {
            builder.and(club.participantTypes.contains(participantType));
        }

        return builder;
    }

    @Override
    public List<ClubRecommendation> findRecommendations(
            EnumSet<ClubInterestCategory> memberCategories,
            LocalDateTime lastActivityAt,
            String memberId,
            int size
    ) {
        // 공통 조건: 공개 여부 + 최근 활동 + 이미 클럽 가입 이력(어떤 상태이든) 제외
        BooleanBuilder builder = new BooleanBuilder()
                .and(isOpen())
                .and(isRecentlyActive(lastActivityAt))
                .and(notRelated(memberId));

        // 관심사가 없으면 겹침 0으로 취급하고 정렬
        if (memberCategories == null || memberCategories.isEmpty()) {
            return queryFactory
                    .select(Projections.constructor(
                            ClubRecommendation.class,
                            club.id.as("clubId"),
                            club.name.as("clubName"),
                            ExpressionUtils.as(Expressions.constant(0L), "overlapCount"),
                            clubMember.id.countDistinct().as("activeMemberCount"),
                            club.lastActivityAt.as("lastActivityAt")
                    ))
                    .from(club)
                    .leftJoin(clubMember)
                    .on(
                            clubMember.club.eq(club),
                            clubMember.clubMemberStatus.in(ClubMemberStatus.activeStatuses())
                    )
                    .where(builder)
                    .groupBy(
                            club.id,
                            club.name,
                            club.lastActivityAt
                    )
                    .orderBy(
                            clubMember.id.countDistinct().desc(),
                            club.lastActivityAt.desc(),
                            club.id.desc()
                    )
                    .limit(size)
                    .fetch();
        }

        // 관심사가 있으면 겹침 수 계산 후 정렬
        EnumPath<ClubInterestCategory> interest =
                Expressions.enumPath(ClubInterestCategory.class, "interest");

        NumberExpression<Long> overlapCountExpr = new CaseBuilder()
                .when(interest.in(memberCategories))
                .then(interest)
                .otherwise((ClubInterestCategory) null)
                .countDistinct();

        return queryFactory
                .select(Projections.constructor(
                        ClubRecommendation.class,
                        club.id.as("clubId"),
                        club.name.as("clubName"),
                        overlapCountExpr.as("overlapCount"),
                        clubMember.id.countDistinct().as("activeMemberCount"),
                        club.lastActivityAt.as("lastActivityAt")
                ))
                .from(club)
                .leftJoin(club.interestCategories, interest)
                .leftJoin(clubMember)
                .on(
                        clubMember.club.eq(club),
                        clubMember.clubMemberStatus.in(ClubMemberStatus.activeStatuses())
                )
                .where(
                        builder
                )
                .groupBy(
                        club.id,
                        club.name,
                        club.lastActivityAt
                )
                .orderBy(
                        overlapCountExpr.desc(),
                        clubMember.id.countDistinct().desc(),
                        club.lastActivityAt.desc(),
                        club.id.desc()
                )
                .limit(size)
                .fetch();
    }

    // ========== 조건식들 ==========
    private BooleanExpression isOpen() {
        return club.isOpen.isTrue();
    }

    private BooleanExpression isRecentlyActive(LocalDateTime lastActivityAt) {
        return club.lastActivityAt.goe(lastActivityAt);
    }

    private BooleanExpression notRelated(String memberId) {
        QClubMember subClubMember = new QClubMember("subClubMember");
        return JPAExpressions.selectOne()
                .from(subClubMember)
                .where(subClubMember.club.id.eq(club.id),
                        subClubMember.memberId.eq(memberId)
                )
                .notExists();
    }
}