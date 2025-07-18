package checkmo.domain.category.facade;

import checkmo.domain.category.web.dto.CategoryRequestDTO;
import checkmo.global.dto.CategorySharedDTO;

/**
 * Category Domain Command Facade
 *
 * Category 도메인의 Command(수정) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 * 실용적인 관점에서, Command 실행 후의 최종 상태를 공유 DTO에 담아 반환합니다.
 */
public interface CategoryCommandFacade {

    /**
     * 특정 회원의 관심 카테고리 목록을 수정합니다.
     *
     * @param memberId 회원 ID
     * @param request  수정할 카테고리 ID 목록 DTO
     * @return 수정된 카테고리 정보가 담긴 **공유 DTO**
     */
    CategorySharedDTO.CategoryInfoListDTO modifyMemberCategories(Long memberId, CategoryRequestDTO.CategoryListRequestDTO request);

    /**
     * 특정 모임의 카테고리 목록을 수정합니다.
     *
     * @param clubId  모임 ID
     * @param request 수정할 카테고리 ID 목록 DTO
     * @return 수정된 카테고리 정보가 담긴 **공유 DTO**
     */
    CategorySharedDTO.CategoryInfoListDTO modifyClubCategories(Long clubId, CategoryRequestDTO.CategoryListRequestDTO request);
}