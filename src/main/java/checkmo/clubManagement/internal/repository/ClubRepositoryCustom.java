package checkmo.clubManagement.internal.repository;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubInterestCategory;
import checkmo.clubManagement.internal.repository.projection.ClubRecommendation;
import checkmo.clubManagement.internal.repository.projection.ClubSitemapProjection;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

public interface ClubRepositoryCustom {
    List<Club> searchClubs(ClubRequestDTO.ClubSearchFilter filter, Long cursorId, Integer size);

    List<ClubSitemapProjection> findOpenClubSitemapItems(Long cursorId, Integer size);

    List<ClubRecommendation> findRecommendations(
            EnumSet<ClubInterestCategory> memberCategories,
            LocalDateTime lastActivityAt,
            Long memberId,
            int size
    );
}
