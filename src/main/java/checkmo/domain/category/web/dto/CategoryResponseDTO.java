package checkmo.domain.category.web.dto;

import checkmo.domain.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class CategoryResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryListResponseDTO {
        List<CategoryInfoResponseDTO> categoryList; // 카테고리 ID 리스트
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryInfoResponseDTO {
        private Long id; // 카테고리 ID
        private String name; // 카테고리 이름
    }
}
