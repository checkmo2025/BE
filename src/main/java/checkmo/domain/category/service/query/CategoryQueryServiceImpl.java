package checkmo.domain.category.service.query;

import checkmo.domain.category.converter.CategoryConverter;
import checkmo.domain.category.entity.ClubCategory;
import checkmo.domain.category.repository.ClubCategoryRepository;
import checkmo.domain.category.web.dto.CategoryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryQueryServiceImpl implements CategoryQueryService {

    private final ClubCategoryRepository clubCategoryRepository;

    @Override
    public CategoryResponseDTO.CategoryListResponseDTO findAllCategories() {
        return null;
    }

    @Override
    public CategoryResponseDTO.CategoryListResponseDTO findCategoriesByMember(Long memberId) {
        return null;
    }

    /**
     * 모임의 모든 카테고리 조회
     *
     * @param clubId 모임 ID
     * @return 모임의 카테고리 정보가 담긴 List
     */
    @Override
    public CategoryResponseDTO.CategoryListResponseDTO findCategoriesByClub(Long clubId) {
        // clubId에 해당하는 ClubCategory 리스트 조회
        List<ClubCategory> clubCategories = clubCategoryRepository.findByClubId(clubId);

        // DTO로 변환 후 반환
        return CategoryConverter.toCategoryListResponseDTO(clubCategories);
    }

    /**
     * 여러 클럽의 카테고리 목록을 한꺼번에 조회합니다.
     *
     * @param clubIds 모임 ID 리스트
     * @return 클럽 ID별 카테고리 정보 매핑
     */
    @Override
    public Map<Long, CategoryResponseDTO.CategoryListResponseDTO> findCategoriesByClubs(List<Long> clubIds) {

        // clubIds에 해당하는 ClubCategory를 한 번에 조회
        List<ClubCategory> clubCategories = clubCategoryRepository.findByClubIdIn(clubIds);

        // clubId별로 그룹핑
        Map<Long, List<ClubCategory>> grouped = clubCategories.stream()
                .collect(Collectors.groupingBy(ClubCategory::getClubId));

        // Map<Long, CategoryListResponseDTO> 변환
        Map<Long, CategoryResponseDTO.CategoryListResponseDTO> result = new HashMap<>();
        for (Map.Entry<Long, List<ClubCategory>> entry : grouped.entrySet()) {
            result.put(entry.getKey(), CategoryConverter.toCategoryListResponseDTO(entry.getValue()));
        }

        return result;
    }

}
