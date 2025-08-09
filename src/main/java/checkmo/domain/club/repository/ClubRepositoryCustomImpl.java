package checkmo.domain.club.repository;

import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.QClub;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ClubRepositoryCustomImpl implements ClubRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QClub club = QClub.club;

    // 검색을 위한 메서드
    @Override
    public List<Club> searchClubs(String keyword, int region, int participants, Long cursorId, Integer size) {

        // 검색 조건 빌더 생성
        BooleanBuilder builder = buildSearchCondition(keyword, region, participants);

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
    private BooleanBuilder buildSearchCondition(String keyword, int region, int participants) {

        BooleanBuilder builder = new BooleanBuilder();

        // 키워드가 비어있지 않은 경우에만 검색 조건 추가
        if (StringUtils.hasText(keyword)) {
            BooleanBuilder keywordBuilder = new BooleanBuilder();

            // region 필터가 켜졌을 경우
            if (region == 1) {
                keywordBuilder.or(club.region.containsIgnoreCase(keyword));
            }

            // participants 필터가 켜졌을 경우
            if (participants == 1) {
                keywordBuilder.or(club.participantTypes.any().stringValue().containsIgnoreCase(keyword));
            }

            // 둘 다 꺼졌을 경우 (region == 0 & participants == 0)
            if (region == 0 && participants == 0) {
                keywordBuilder.or(club.name.containsIgnoreCase(keyword))
                        .or(club.region.containsIgnoreCase(keyword))
                        .or(club.participantTypes.any().stringValue().containsIgnoreCase(keyword));
            }

            builder.and(keywordBuilder);
        }

        return builder;
    }

}