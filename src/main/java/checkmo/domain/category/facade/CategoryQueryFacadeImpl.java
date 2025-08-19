package checkmo.domain.category.facade;

import checkmo.domain.category.converter.CategoryConverter;
import checkmo.domain.category.entity.Category;
import checkmo.domain.category.service.query.CategoryQueryService;
import checkmo.global.dto.CategorySharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryQueryFacadeImpl implements CategoryQueryFacade {

    // 자신의 QueryService
    private final CategoryQueryService categoryQueryService;

    @Override
    public CategorySharedDTO.CategoryInfoList getAllCategoriesForShare() {
        // Service에서 Category 엔티티 받아서 직접 변환
        List<Category> categories = categoryQueryService.findAllCategories();

        return CategoryConverter.fromCategoriesToCategoryInfoList(categories);
    }
}
