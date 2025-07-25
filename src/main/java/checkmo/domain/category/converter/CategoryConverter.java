package checkmo.domain.category.converter;

import checkmo.domain.category.entity.ClubCategory;
import checkmo.domain.category.entity.MemberCategory;
import checkmo.domain.category.web.dto.CategoryResponseDTO;
import checkmo.global.dto.CategorySharedDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryConverter {

    /**
     * CategoryListResponseDTO → CategoryInfoListDTO 변환
     */
    public static CategorySharedDTO.CategoryInfoListDTO toCategoryInfoListDTO(
            CategoryResponseDTO.CategoryListResponseDTO response
    ) {
        List<CategorySharedDTO.CategoryInfoDTO> categoryList = response.getCategoryList().stream()
                .map(CategoryConverter::toCategoryInfoDTO)
                .collect(Collectors.toList());

        return CategorySharedDTO.CategoryInfoListDTO.builder()
                .categoryList(categoryList)
                .build();
    }

    /**
     * CategoryInfoResponseDTO → CategoryInfoDTO 변환
     */
    public static CategorySharedDTO.CategoryInfoDTO toCategoryInfoDTO(
            CategoryResponseDTO.CategoryInfoResponseDTO dto
    ) {
        return CategorySharedDTO.CategoryInfoDTO.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }

    /**
     * ClubCategory 엔티티 → CategoryInfoResponseDTO 변환
     *
     * @param cc ClubCategory 엔티티
     * @return CategoryInfoResponseDTO
     */
    public static CategoryResponseDTO.CategoryInfoResponseDTO toCategoryInfoResponseDTO(ClubCategory cc) {
        return CategoryResponseDTO.CategoryInfoResponseDTO.builder()
                .id(cc.getCategory().getId())
                .name(cc.getCategory().getName())
                .build();
    }

    /**
     * List<ClubCategory> → CategoryListResponseDTO 변환
     *
     * @param ccList ClubCategory 엔티티 리스트
     * @return CategoryListResponseDTO
     */
    public static CategoryResponseDTO.CategoryListResponseDTO toCategoryListResponseDTO(List<ClubCategory> ccList) {
        List<CategoryResponseDTO.CategoryInfoResponseDTO> dtoList = ccList.stream()
                .map(CategoryConverter::toCategoryInfoResponseDTO)
                .collect(Collectors.toList());

        return CategoryResponseDTO.CategoryListResponseDTO.builder()
                .categoryList(dtoList)
                .build();
    }

    public static CategoryResponseDTO.CategoryListResponseDTO toMemberCategoryListResponseDTO(List<MemberCategory> mcList) {
        List<CategoryResponseDTO.CategoryInfoResponseDTO> dtoList = mcList.stream()
                                                                          .map(mc -> CategoryResponseDTO.CategoryInfoResponseDTO.builder()
                                                                                                                                .id(mc.getCategory().getId())
                                                                                                                                .name(mc.getCategory().getName())
                                                                                                                                .build())
                                                                          .collect(Collectors.toList());

        return CategoryResponseDTO.CategoryListResponseDTO.builder()
                                                          .categoryList(dtoList)
                                                          .build();
    }

}
