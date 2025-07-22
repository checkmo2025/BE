package checkmo.domain.category.service.command.impl;

import checkmo.domain.category.converter.CategoryConverter;
import checkmo.domain.category.entity.Category;
import checkmo.domain.category.entity.ClubCategory;
import checkmo.domain.category.repository.CategoryRepository;
import checkmo.domain.category.repository.ClubCategoryRepository;
import checkmo.domain.category.service.command.CategoryAssignmentCommandService;
import checkmo.domain.category.web.dto.CategoryRequestDTO;
import checkmo.domain.category.web.dto.CategoryResponseDTO;
import checkmo.domain.club.entity.Club;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryAssignmentCommandServiceImpl implements CategoryAssignmentCommandService {

    private final CategoryRepository categoryRepository;
    private final ClubCategoryRepository clubCategoryRepository;

    @Override
    public CategoryResponseDTO.CategoryListResponseDTO modifyMemberCategories(
            Long memberId,
            CategoryRequestDTO.CategoryListRequestDTO request
    ) {
        return null;
    }

    @Override
    public CategoryResponseDTO.CategoryListResponseDTO modifyClubCategories(
            Long clubId,
            CategoryRequestDTO.CategoryListRequestDTO request
    ) {
        // 1. 현재 clubId에 등록된 카테고리 목록 조회
        List<ClubCategory> existingCategories = clubCategoryRepository.findByClubId(clubId);

        // 2. 요청으로 받은 카테고리 ID 리스트
        List<Long> requestedCategoryIds = request.getCategoryId();

        // 3. 기존 카테고리 ID 목록만 추출
        List<Long> existingCategoryIds = existingCategories.stream()
                .map(cc -> cc.getCategory().getId())
                .toList();

        // 4. 삭제할 카테고리 = 기존에 있지만 요청 목록에 없는 것
        List<Long> categoriesToRemove = existingCategoryIds.stream()
                .filter(id -> !requestedCategoryIds.contains(id))
                .toList();

        // 5. 삭제 처리
        if (!categoriesToRemove.isEmpty()) {
            clubCategoryRepository.deleteByClubIdAndCategoryIds(clubId, categoriesToRemove);
        }

        // 6. 새로 추가할 카테고리 = 요청 목록 중 기존에 없는 것
        List<Long> categoriesToAdd = requestedCategoryIds.stream()
                .filter(id -> !existingCategoryIds.contains(id))
                .toList();

        // 7. 새로운 카테고리 추가
        if (!categoriesToAdd.isEmpty()) {
            List<Category> categoriesToAddEntities = categoryRepository.findAllById(categoriesToAdd);
            Club club = Club.builder().id(clubId).build(); // Club 엔티티 더미 객체 생성

            categoriesToAddEntities.forEach(category -> {
                ClubCategory clubCategory = ClubCategory.builder()
                        .club(club)
                        .category(category)
                        .build();
                clubCategoryRepository.save(clubCategory);
            });
        }

        // 8. 최종 카테고리 목록 조회 및 DTO 변환
        List<ClubCategory> finalCategories = clubCategoryRepository.findByClubId(clubId);
        List<CategoryResponseDTO.CategoryInfoResponseDTO> categoryDTOList = CategoryConverter.toCategoryInfoResponseDTOList(finalCategories);

        return CategoryResponseDTO.CategoryListResponseDTO.builder()
                .categoryList(categoryDTOList)
                .build();

    }

}
