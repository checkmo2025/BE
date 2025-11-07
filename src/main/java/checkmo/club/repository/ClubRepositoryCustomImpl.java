package checkmo.club.repository;

import checkmo.club.entity.Club;
import checkmo.club.web.dto.club.ClubRequestDTO;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static checkmo.club.entity.QClub.club;

@Repository
@RequiredArgsConstructor
public class ClubRepositoryCustomImpl implements ClubRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 검색을 위한 메서드
    @Override
    public List<Club> searchClubs(ClubRequestDTO.ClubSearchFilter filter, Long cursorId, Integer size) {

        // 검색 조건 빌더 생성
        BooleanBuilder builder = buildSearchCondition(filter);

        // 커서 ID가 null이 아니고 0이 아닐 경우, 커서 ID보다 작은 ID를 가진 클럽만 조회
        if (cursorId != null && cursorId != 0L) {
            builder.and(club.id.lt(cursorId));
        }

        // 쿼리 실행
        return queryFactory
                .selectFrom(club)
                .where(builder)
                .orderBy(club.id.desc())
                .limit(size + 1)
                .fetch();
    }

    // 검색 조건 빌더
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