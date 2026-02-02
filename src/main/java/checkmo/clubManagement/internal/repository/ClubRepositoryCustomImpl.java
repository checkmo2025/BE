package checkmo.clubManagement.internal.repository;

import static checkmo.clubManagement.internal.entity.QClub.club;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubParticipantType;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubRequestDTO.ClubSearchOutputFilter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
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

}