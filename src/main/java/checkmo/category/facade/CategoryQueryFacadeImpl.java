package checkmo.category.facade;

import checkmo.category.converter.CategoryConverter;
import checkmo.category.entity.Category;
import checkmo.category.service.query.CategoryQueryService;
import checkmo.category.CategorySharedDTO;
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
