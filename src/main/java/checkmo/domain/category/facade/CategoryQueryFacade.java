package checkmo.domain.category.facade;

import checkmo.global.dto.CategorySharedDTO;

/**
 * Category Domain Query Facade
 * Level 1 Domain (Category) - 카테고리 자체 정보 제공만 담당
 * 이 도메인의 모든 조회 기능은 다른 도메인에 카테고리 정보를 제공하기 위한 것입니다.
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