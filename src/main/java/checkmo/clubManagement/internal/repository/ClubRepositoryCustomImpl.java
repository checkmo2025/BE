package checkmo.clubManagement.internal.repository;

import static checkmo.clubManagement.internal.entity.QClub.club;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class ClubRepositoryCustomImpl implements ClubRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 검색을 위한 메서드
    @Override
    public List<Club> searchClubs(ClubRequestDTO.ClubSearchFilter filter, Long cursorId, Integer size) {
        BooleanBuilder builder = buildSearchCondition(filter);

        if (cursorId != null && cursorId != 0L) {
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

        String keyword = filter.keyword();
        int name = filter.name();
        int region = filter.region();
        int participants = filter.participants();

        // 키워드가 비어있지 않은 경우에만 검색 조건 추가
        if (StringUtils.hasText(keyword)) {
            BooleanBuilder keywordBuilder = new BooleanBuilder();

            // 조합에 따른 조건 처리
            if (name == 1) {
                keywordBuilder.or(club.name.containsIgnoreCase(keyword));
            }
            if (region == 1) {
                keywordBuilder.or(club.region.containsIgnoreCase(keyword));
            }
            if (participants == 1) {
                keywordBuilder.or(club.participantTypes.any().stringValue().containsIgnoreCase(keyword));
            }

            // 세 플래그가 전부 0인 경우 → 전체 필드 검색
            if (name == 0 && region == 0 && participants == 0) {
                keywordBuilder.or(club.name.containsIgnoreCase(keyword))
                        .or(club.region.containsIgnoreCase(keyword))
                        .or(club.participantTypes.any().stringValue().containsIgnoreCase(keyword));
            }

            builder.and(keywordBuilder);
        }

        return builder;
    }

}