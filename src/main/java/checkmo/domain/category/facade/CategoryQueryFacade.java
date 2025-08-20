package checkmo.domain.category.facade;

import checkmo.global.dto.CategorySharedDTO;

/**
 * 카테고리 자체 정보 제공만 담당
 * 그런데 이게 필요한가....??
 */
public interface CategoryQueryFacade {

    /**
     * 모든 카테고리 목록을 조회합니다. (외부용)
     * 카테고리 선택 UI 등에서 사용됩니다.
     *
     * @return 모든 카테고리 정보가 담긴 공유 DTO
     */
    CategorySharedDTO.CategoryInfoList getAllCategoriesForShare();

}