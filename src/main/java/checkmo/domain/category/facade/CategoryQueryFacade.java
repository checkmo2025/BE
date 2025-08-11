package checkmo.domain.category.facade;

import checkmo.domain.category.entity.Category;
import checkmo.global.dto.CategorySharedDTO;

import java.util.List;
import java.util.Map;

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
    CategorySharedDTO.CategoryInfoList getAllCategoriesForShare();

    /**
     * 특정 회원이 설정한 카테고리 목록을 조회합니다. (외부용)
     * 마이페이지 등 다른 서비스에서 회원의 관심 카테고리를 표시할 때 사용됩니다.
     *
     * @param memberId 회원 ID
     * @return 해당 회원의 카테고리 정보가 담긴 공유 DTO
     */
    CategorySharedDTO.CategoryInfoList getCategoriesByMemberForShare(String memberId);

    /**
     * 특정 모임에 설정된 카테고리 목록을 조회합니다. (외부용)
     * 모임 상세 정보 등에서 모임의 카테고리를 표시할 때 사용됩니다.
     *
     * @param clubId 모임 ID
     * @return 해당 모임의 카테고리 정보가 담긴 공유 DTO
     */
    CategorySharedDTO.CategoryInfoList getCategoriesByClubForShare(Long clubId);

    /**
     * 여러 클럽에 설정된 카테고리 목록을 한꺼번에 조회합니다. (외부용)
     *
     * @param clubIds 클럽 ID 리스트
     * @return 클럽 ID별 카테고리 정보 리스트 매핑
     */
    Map<Long, List<CategorySharedDTO.CategoryInfo>> getCategoriesByClubs(List<Long> clubIds);

    /**
     * 다른 도메인에서 관계 설정을 위해 엔티티의 프록시(참조)를 조회합니다. (외부용)
     * ‼️ 이 메소드는 실제 DB 조회를 발생시키지 않는 메소드!!!
     * ‼️ 그리고 반드시 외래 키를 설정하는 용도로만 사용되어야 함!
     *
     * 이 메소드는 구현할 때 단순히
     * {@code return categoryRepository.getReferenceById(categoryId);}만 하면 됨
     *
     * @param categoryId 참조할 카테고리의 ID
     * @return Category 엔티티의 프록시 객체
     */
    Category findCategoryReferenceById(Long categoryId);

}