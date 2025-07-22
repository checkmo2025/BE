package checkmo.domain.category.converter;

import checkmo.domain.category.entity.ClubCategory;
import checkmo.domain.category.web.dto.CategoryResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryConverter {

    // =====================================================
    // Entity → DTO 변환
    // =====================================================

    /**
     * ClubCategory 리스트를 CategoryInfoResponseDTO 리스트로 변환
     */
    public static List<CategoryResponseDTO.CategoryInfoResponseDTO> toCategoryInfoResponseDTOList(List<ClubCategory> clubCategories) {
        return clubCategories.stream()
                .map(cc -> CategoryResponseDTO.CategoryInfoResponseDTO.builder()
                        .id(cc.getCategory().getId())
                        .name(cc.getCategory().getName())
                        .build())
                .collect(Collectors.toList());
    }

}
