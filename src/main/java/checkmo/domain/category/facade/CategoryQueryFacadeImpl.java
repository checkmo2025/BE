package checkmo.domain.category.facade;

import checkmo.domain.category.entity.Category;
import checkmo.domain.category.repository.CategoryRepository;
import checkmo.global.dto.CategorySharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryQueryFacadeImpl implements CategoryQueryFacade {

    private final CategoryRepository categoryRepository; // 프록시용

    @Override
    public CategorySharedDTO.CategoryInfoListDTO getAllCategoriesForShare() {
        return null;
    }

    @Override
    public CategorySharedDTO.CategoryInfoListDTO getCategoriesByMemberForShare(Long memberId) {
        return null;
    }

    @Override
    public CategorySharedDTO.CategoryInfoListDTO getCategoriesByClubForShare(Long clubId) {
        return null;
    }

    @Override
    public Category findCategoryReferenceById(Long categoryId) {
        // 프록시 조회 - 실제 DB 조회 안함
        return categoryRepository.getReferenceById(categoryId);
    }
}
