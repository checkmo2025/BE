package checkmo.category.internal.converter;

import checkmo.category.CategoryExternalDTO;
import checkmo.category.internal.entity.Category;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryConverter {

    // =====================================================
    // Entity → SharedDTO 변환
    // =====================================================

    /**
     * List<Category> → CategoryInfoList
     */
    public static CategoryExternalDTO.CategoryInfoList fromCategoriesToCategoryInfoList(List<Category> categories) {
        List<CategoryExternalDTO.CategoryInfo> categoryList = categories.stream()
                .map(category -> CategoryExternalDTO.CategoryInfo.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .collect(Collectors.toList());

        return CategoryExternalDTO.CategoryInfoList.builder()
                .categoryList(categoryList)
                .build();
    }
}
