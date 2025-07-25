package checkmo.domain.category.service.query;

import checkmo.domain.category.converter.CategoryConverter;
import checkmo.domain.category.entity.ClubCategory;
import checkmo.domain.category.repository.ClubCategoryRepository;
import checkmo.domain.category.web.dto.CategoryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

}
