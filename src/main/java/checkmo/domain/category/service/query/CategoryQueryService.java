package checkmo.domain.category.service.query;

import checkmo.domain.category.web.dto.CategoryResponseDTO;

import java.util.List;
import java.util.Map;

/**
 * 카테고리 조회 서비스
 */
public interface CategoryQueryService {

    /**
     * 모든 카테고리 조회
     *
     * @return 모든 카테고리가 담긴 List
     */
    CategoryResponseDTO.CategoryListResponseDTO findAllCategories();

    /**
     * 회원이 설정한 모든 카테고리 조회
     *
     * @param memberId 회원 ID
     * @return 회원의 카테고리 정보가 담긴 List
     */
    CategoryResponseDTO.CategoryListResponseDTO findCategoriesByMember(String memberId);

    /**
     * 모임의 모든 카테고리 조회
     *
     * @param clubId 모임 ID
     * @return 모임의 카테고리 정보가 담긴 List
     */
    CategoryResponseDTO.CategoryListResponseDTO findCategoriesByClub(Long clubId);

    /**
     * 여러 클럽의 카테고리 목록을 한꺼번에 조회합니다.
     *
     * @param clubIds 모임 ID 리스트
     * @return 클럽 ID별 카테고리 정보 매핑
     */
    Map<Long, CategoryResponseDTO.CategoryListResponseDTO> findCategoriesByClubs(List<Long> clubIds);

}
