package checkmo.domain.club.service.command;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.category.entity.Category;
import checkmo.domain.category.repository.CategoryRepository;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubCategory;
import checkmo.domain.club.repository.ClubCategoryRepository;
import checkmo.domain.club.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubCategoryCommandServiceImpl implements ClubCategoryCommandService {

    private final ClubRepository clubRepository;
    private final ClubCategoryRepository clubCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void modifyClubCategories(Long clubId, List<Long> categoryIds) {
        // 1. Club 엔티티 조회
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_NOT_FOUND));

        // 2. 기존 ClubCategory 목록 조회
        List<ClubCategory> existingClubCategories = clubCategoryRepository.findByClubId(clubId);

        // 3. 기존 카테고리 ID 리스트
        List<Long> existingCategoryIds = existingClubCategories.stream()
                .map(cc -> cc.getCategory().getId())
                .toList();

        // 4. 추가할 카테고리
        List<Long> categoriesToAdd = categoryIds.stream()
                .filter(id -> !existingCategoryIds.contains(id))
                .toList();

        // 5. 제거할 카테고리
        List<Long> categoriesToRemove = existingCategoryIds.stream()
                .filter(id -> !categoryIds.contains(id))
                .toList();

        // 6. 추가
        for (Long categoryId : categoriesToAdd) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.CATEGORY_NOT_FOUND));

            ClubCategory newClubCategory = ClubCategory.builder()
                    .club(club)
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

        // 8. 최종 ClubCategory 목록 반환
        clubCategoryRepository.findByClubId(clubId);
    }
}