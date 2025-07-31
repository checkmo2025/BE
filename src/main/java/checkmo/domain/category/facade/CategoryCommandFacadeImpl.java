package checkmo.domain.category.facade;

import checkmo.domain.category.converter.CategoryConverter;
import checkmo.domain.category.service.command.CategoryAssignmentCommandService;
import checkmo.domain.category.web.dto.CategoryRequestDTO;
import checkmo.domain.category.web.dto.CategoryResponseDTO;
import checkmo.global.dto.CategorySharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class CategoryCommandFacadeImpl implements CategoryCommandFacade {

    private final CategoryAssignmentCommandService categoryAssignmentCommandService;

    @Override
    public CategorySharedDTO.CategoryInfoList modifyMemberCategories(String memberId, CategorySharedDTO.CategoryIdListDTO request) {
        CategoryResponseDTO.CategoryListResponseDTO response = categoryAssignmentCommandService.modifyMemberCategories(memberId, request);
        return CategoryConverter.toCategoryInfoListDTO(response);
    }

    /**
     * 특정 모임의 카테고리 목록을 수정합니다.
     *
     * @param clubId  모임 ID
     * @param request 수정할 카테고리 ID 목록 DTO
     * @return 수정된 카테고리 정보가 담긴 **공유 DTO**
     */
    @Override
    public CategorySharedDTO.CategoryInfoList modifyClubCategories(Long clubId, CategoryRequestDTO.CategoryListRequestDTO request) {
        CategoryResponseDTO.CategoryListResponseDTO response = categoryAssignmentCommandService.modifyClubCategories(clubId, request);
        return CategoryConverter.toCategoryInfoListDTO(response);
    }
}
