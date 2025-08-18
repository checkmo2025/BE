package checkmo.domain.category.facade;

import checkmo.domain.category.converter.CategoryConverter;
import checkmo.domain.category.entity.ClubCategory;
import checkmo.domain.category.service.command.CategoryAssignmentCommandService;
import checkmo.global.dto.CategorySharedDTO;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class CategoryCommandFacadeImpl implements CategoryCommandFacade {

    private final CategoryAssignmentCommandService categoryAssignmentCommandService;

    @Override
    public void modifyMemberCategories(String memberId, CategorySharedDTO.CategoryIdListDTO request) {
        // DTO에서 데이터 추출하여 Service에 전달
        categoryAssignmentCommandService.modifyMemberCategories(memberId, request.getCategoryIdList());
    }

    /**
     * 특정 모임의 카테고리 목록을 수정합니다.
     *
     * @param clubId  모임 ID
     * @param request 수정할 카테고리 ID 목록 DTO
     * @return 수정된 카테고리 정보가 담긴 **공유 DTO**
     */
    @Override
    public CategorySharedDTO.CategoryInfoList modifyClubCategories(Long clubId, CategorySharedDTO.CategoryIdListDTO request) {
        // DTO에서 데이터 추출하여 Service에 전달
        List<ClubCategory> clubCategories =
                categoryAssignmentCommandService.modifyClubCategories(clubId, request.getCategoryIdList());

        return CategoryConverter.fromClubCategoriesToCategoryInfoList(clubCategories);
    }
}
