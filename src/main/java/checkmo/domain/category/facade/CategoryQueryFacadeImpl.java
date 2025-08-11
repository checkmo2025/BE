package checkmo.domain.category.facade;

import checkmo.domain.category.converter.CategoryConverter;
import checkmo.domain.category.entity.Category;
import checkmo.domain.category.repository.CategoryRepository;
import checkmo.domain.category.service.query.CategoryQueryService;
import checkmo.domain.category.web.dto.CategoryResponseDTO;
import checkmo.global.dto.CategorySharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryQueryFacadeImpl implements CategoryQueryFacade {

    private final CategoryRepository categoryRepository; // 프록시용
    private final CategoryQueryService categoryQueryService;

    @Override
    public CategorySharedDTO.CategoryInfoList getAllCategoriesForShare() {
        return null;
    }

    @Override
    public CategorySharedDTO.CategoryInfoList getCategoriesByMemberForShare(String memberId) {
        CategoryResponseDTO.CategoryListResponseDTO responseDTO = categoryQueryService.findCategoriesByMember(memberId);
        return CategoryConverter.toCategoryInfoListDTO(responseDTO);
    }

    /**
     * 특정 모임에 설정된 카테고리 목록을 조회합니다. (외부용)
     * 모임 상세 정보 등에서 모임의 카테고리를 표시할 때 사용됩니다.
     *
     * @param clubId 모임 ID
     * @return 해당 모임의 카테고리 정보가 담긴 공유 DTO
     */
    @Override
    public CategorySharedDTO.CategoryInfoList getCategoriesByClubForShare(Long clubId) {
        CategoryResponseDTO.CategoryListResponseDTO responseDTO = categoryQueryService.findCategoriesByClub(clubId);
        return CategoryConverter.toCategoryInfoListDTO(responseDTO);
    }

    /**
     * 여러 클럽에 설정된 카테고리 목록을 한꺼번에 조회합니다. (외부용)
     *
     * @param clubIds 클럽 ID 리스트
     * @return 클럽 ID별 카테고리 정보 리스트 매핑
     */
    @Override
    public Map<Long, List<CategorySharedDTO.CategoryInfo>> getCategoriesByClubs(List<Long> clubIds) {

        // 1. 여러 클럽의 카테고리 정보를 한꺼번에 조회
        Map<Long, CategoryResponseDTO.CategoryListResponseDTO> responseMap = categoryQueryService.findCategoriesByClubs(clubIds);

        // 2. DTO 변환 및 Map<Long, List<CategoryInfo>> 형태로 변환
        return responseMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> CategoryConverter.toCategoryInfoListDTO(entry.getValue()).getCategoryList()
                ));
    }

    @Override
    public Category findCategoryReferenceById(Long categoryId) {
        // 프록시 조회 - 실제 DB 조회 안함
        return categoryRepository.getReferenceById(categoryId);
    }
}
