package checkmo.domain.category.service.query;

import checkmo.domain.category.dto.CategoryResponseDTO;

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
    CategoryResponseDTO.CategoryListResponseDTO findCategoriesByMember(Long memberId);

    /**
     * 모임의 모든 카테고리 조회
     *
     * @param clubId 모임 ID
     * @return 모임의 카테고리 정보가 담긴 List
     */
    CategoryResponseDTO.CategoryListResponseDTO findCategoriesByClub(Long clubId);
}
