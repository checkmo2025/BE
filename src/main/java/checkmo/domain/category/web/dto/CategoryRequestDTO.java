package checkmo.domain.category.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class CategoryRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class CategoryListRequestDTO {
        private List<Long> categoryId;
    }
}
