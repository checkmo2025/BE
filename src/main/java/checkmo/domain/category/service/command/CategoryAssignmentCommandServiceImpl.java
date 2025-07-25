package checkmo.domain.category.service.command;

import checkmo.apiPayload.exception.GeneralException;
import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.domain.category.converter.CategoryConverter;
import checkmo.domain.category.entity.Category;
import checkmo.domain.category.entity.ClubCategory;
import checkmo.domain.category.repository.CategoryRepository;
import checkmo.domain.category.repository.ClubCategoryRepository;
import checkmo.domain.category.web.dto.CategoryRequestDTO;
import checkmo.domain.category.web.dto.CategoryResponseDTO;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.facade.ClubQueryFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryAssignmentCommandServiceImpl implements CategoryAssignmentCommandService {

    private final ClubCategoryRepository clubCategoryRepository;
    private final ClubQueryFacade clubQueryFacade;
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponseDTO.CategoryListResponseDTO modifyMemberCategories(Long memberId, CategoryRequestDTO.CategoryListRequestDTO request) {
        return null;
    }

    /**
     * Club의 관심 카테고리 수정 - 이미 추가되어있는 관심 카테고리는 서비스 로직 구현 시 제외하고 새로운 카테고리만 추가 or 제거
     *
     * @param clubId       모임 ID
     * @param request  추가할 카테고리 ID 목록
     * @return 추가된 카테고리 정보가 담긴 DTO
     */
    @Override
    public CategoryResponseDTO.CategoryListResponseDTO modifyClubCategories(Long clubId, CategoryRequestDTO.CategoryListRequestDTO request) {

        // 1. 기존 ClubCategory 목록 조회
        List<ClubCategory> existingClubCategories = clubCategoryRepository.findByClubId(clubId);

        // 2. 기존 카테고리 ID 리스트
        List<Long> existingCategoryIds = existingClubCategories.stream()
                .map(cc -> cc.getCategory().getId())
                .toList();

        // 3. 요청 카테고리 ID 리스트
        List<Long> requestedCategoryIds = request.getCategoryIdList();

        // 4. 추가할 카테고리
        List<Long> categoriesToAdd = requestedCategoryIds.stream()
                .filter(id -> !existingCategoryIds.contains(id))
                .toList();

        // 5. 제거할 카테고리
        List<Long> categoriesToRemove = existingCategoryIds.stream()
                .filter(id -> !requestedCategoryIds.contains(id))
                .toList();

        // 6. 추가
        for (Long categoryId : categoriesToAdd) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.CATEGORY_NOT_FOUND));

            Club clubProxy = clubQueryFacade.findClubReferenceById(clubId);

            ClubCategory newClubCategory = ClubCategory.builder()
                    .club(clubProxy)
                    .category(category)
                    .build();

            clubCategoryRepository.save(newClubCategory);
        }

        // 7. 제거
        categoriesToRemove.forEach(categoryId -> {
            existingClubCategories.stream()
                    .filter(cc -> cc.getCategory().getId().equals(categoryId))
                    .findFirst()
                    .ifPresent(clubCategoryRepository::delete);
        });

        // 8. 최종 카테고리 목록
        List<ClubCategory> updatedClubCategories = clubCategoryRepository.findByClubId(clubId);

        // 9. DTO 변환 후 반환
        return CategoryConverter.toCategoryListResponseDTO(updatedClubCategories);

    }

}
