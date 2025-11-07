package checkmo.category.converter;

import checkmo.category.entity.Category;
import checkmo.category.CategorySharedDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryConverter {

    // =====================================================
    // Entity → SharedDTO 변환
    // =====================================================

    /**
     * List<Category> → CategoryInfoList
     */
    public static CategorySharedDTO.CategoryInfoList fromCategoriesToCategoryInfoList(List<Category> categories) {
        List<CategorySharedDTO.CategoryInfo> categoryList = categories.stream()
                .map(category -> CategorySharedDTO.CategoryInfo.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .collect(Collectors.toList());

        return CategorySharedDTO.CategoryInfoList.builder()
                .categoryList(categoryList)
                .build();
    }
}
