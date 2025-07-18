package checkmo.domain.category.facade;

import checkmo.global.dto.CategorySharedDTO;

/**
 * Category Domain Query Facade
 * Category 도메인의 Query(조회) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 * 이 도메인의 모든 조회 기능은 다른 도메인에 정보를 제공하기 위한 것입니다.
 */
public interface CategoryQueryFacade {

    /**
     * 시스템에 존재하는 모든 카테고리 목록을 조회합니다. (외부용)
     * 회원가입, 모임 생성 등에서 카테고리 선택 UI를 구성할 때 사용됩니다.
     *
     * @return 모든 카테고리 정보가 담긴 공유 DTO
     */
    CategorySharedDTO.CategoryInfoListDTO getAllCategoriesForShare();

    /**
     * 특정 회원이 설정한 카테고리 목록을 조회합니다. (외부용)
     * 마이페이지 등 다른 서비스에서 회원의 관심 카테고리를 표시할 때 사용됩니다.
     *
     * @param memberId 회원 ID
     * @return 해당 회원의 카테고리 정보가 담긴 공유 DTO
     */
    CategorySharedDTO.CategoryInfoListDTO getCategoriesByMemberForShare(Long memberId);

    /**
     * 특정 모임에 설정된 카테고리 목록을 조회합니다. (외부용)
     * 모임 상세 정보 등에서 모임의 카테고리를 표시할 때 사용됩니다.
     *
     * @param clubId 모임 ID
     * @return 해당 모임의 카테고리 정보가 담긴 공유 DTO
     */
    CategorySharedDTO.CategoryInfoListDTO getCategoriesByClubForShare(Long clubId);
}