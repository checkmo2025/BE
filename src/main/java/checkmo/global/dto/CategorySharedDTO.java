package checkmo.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class CategorySharedDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryInfoListDTO {
        private List<CategoryInfoDTO> categoryList;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryInfoDTO {
        private Long id;   // 카테고리 ID
        private String name; // 카테고리 이름
    }
}