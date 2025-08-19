package checkmo.domain.club.service.query;

import checkmo.domain.club.entity.ClubCategory;

import java.util.List;

/**
 * 클럽의 카테고리 관련 조회 서비스
 */
public interface ClubCategoryQueryService {

    /**
     * 클럽의 카테고리 목록을 조회합니다.
     * 
     * @param clubId 클럽 ID
     * @return 클럽의 카테고리 목록
     */
    List<ClubCategory> findCategoriesByClub(Long clubId);

    /**
     * 클럽 ID 목록으로 카테고리 목록을 조회합니다.
     * 
     * @param clubIds 클럽 ID 목록
     * @return 클럽의 카테고리 목록
     */
    List<ClubCategory> findCategoriesByClubIds(List<Long> clubIds);
}