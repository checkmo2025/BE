package checkmo.domain.category.facade;

import checkmo.domain.category.converter.CategoryConverter;
import checkmo.domain.category.entity.Category;
import checkmo.domain.category.repository.CategoryRepository;
import checkmo.domain.category.service.query.CategoryQueryService;
import checkmo.domain.category.web.dto.CategoryResponseDTO;
import checkmo.global.dto.CategorySharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryQueryFacadeImpl implements CategoryQueryFacade {

    private final CategoryRepository categoryRepository; // 프록시용
    private final CategoryQueryService categoryQueryService;

    @Override
    public CategorySharedDTO.CategoryInfoList getAllCategoriesForShare() {
        return null;
    }

    @Override
    public CategorySharedDTO.CategoryInfoList getCategoriesByMemberForShare(Long memberId) {
        return null;
    }

    /**
     * 특정 모임에 설정된 카테고리 목록을 조회합니다. (외부용)
     * 모임 상세 정보 등에서 모임의 카테고리를 표시할 때 사용됩니다.
     *
     * @param clubId 모임 ID
     * @return 해당 모임의 카테고리 정보가 담긴 공유 DTO
     */
    @Override
    public CategorySharedDTO.CategoryInfoList getCategoriesByClubForShare(Long clubId) {
        CategoryResponseDTO.CategoryListResponseDTO responseDTO = categoryQueryService.findCategoriesByClub(clubId);
        return CategoryConverter.toCategoryInfoListDTO(responseDTO);
    }

    @Override
    public Category findCategoryReferenceById(Long categoryId) {
        // 프록시 조회 - 실제 DB 조회 안함
        return categoryRepository.getReferenceById(categoryId);
    }
}
