package checkmo.domain.club.service.command;

import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.domain.category.entity.Category;
import checkmo.domain.category.repository.CategoryRepository;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubCategory;
import checkmo.domain.club.repository.ClubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubCategoryCommandServiceImpl implements ClubCategoryCommandService {

    // Domain level 1의 Repository
    private final CategoryRepository categoryRepository;

    // 자신의 Repository
    private final ClubCategoryRepository clubCategoryRepository;

    @Override
    public void createClubCategories(Club club, List<Long> categoryIds) {
        // 0. 요청 가드
        if (categoryIds == null || categoryIds.isEmpty()) return;

        // 1. 추가할 카테고리 엔티티들을 한 번에 조회 (N+1 문제 해결)
        List<Category> categoriesToAdd = categoryRepository.findAllById(categoryIds);

        // 2. 조회한 엔티티 수와 요청한 ID 수가 다르면 예외 발생
        if (categoriesToAdd.size() != categoryIds.size()) {
            throw new GeneralException(ErrorStatus.CATEGORY_NOT_FOUND);
        }

        // 3. ClubCategory 엔티티 생성 및 배치 저장
        List<ClubCategory> newClubCategories = categoriesToAdd.stream()
                .map(category -> ClubCategory.builder()
                        .club(club)
                        .category(category)
                        .build())
                .toList();

        clubCategoryRepository.saveAll(newClubCategories);
    }

    @Override
    public void modifyClubCategories(Club club, List<Long> categoryIds) {
        // 0. 요청 가드
        if (categoryIds == null || categoryIds.isEmpty()) return;

        // 1. 기존 ClubCategory 목록 조회
        List<ClubCategory> existingClubCategories = clubCategoryRepository.findByClubId(club.getId());

        // 2. 기존 카테고리 ID들을 Set으로 변환
        Set<Long> existingCategoryIds = existingClubCategories.stream()
                .map(cc -> cc.getCategory().getId())
                .collect(Collectors.toSet());

        // 3. 변경할 category ID를 Set으로 변환
        Set<Long> newCategoryIds = new HashSet<>(categoryIds);

        // 4. 제거할 카테고리 ID List
        List<ClubCategory> categoriesToRemove = existingClubCategories.stream()
                .filter(cc -> !newCategoryIds.contains(cc.getCategory().getId()))
                .toList();

        // 5. 카테고리 제거 (Batch Delete)
        if (!categoriesToRemove.isEmpty()) {
            clubCategoryRepository.deleteAllInBatch(categoriesToRemove);
        }

        // 6. 추가할 카테고리 ID List
        List<Long> categoriesToAddIds = newCategoryIds.stream()
                .filter(id -> !existingCategoryIds.contains(id))
                .toList();

        // 7. 추가할 카테고리 엔티티들을 한 번에 조회 (N+1 문제 해결)
        if (!categoriesToAddIds.isEmpty()) {
            List<Category> categoriesToAdd = categoryRepository.findAllById(categoriesToAddIds);

            // ID로 조회한 엔티티 수와 요청한 ID 수가 다르면 예외 발생
            if (categoriesToAdd.size() != categoriesToAddIds.size()) {
                throw new GeneralException(ErrorStatus.CATEGORY_NOT_FOUND);
            }

            // 8. ClubCategory 엔티티 생성 및 배치 저장
            List<ClubCategory> newClubCategories = categoriesToAdd.stream()
                    .map(category -> ClubCategory.builder()
                            .club(club)
                            .category(category)
                            .build())
                    .toList();

            clubCategoryRepository.saveAll(newClubCategories);
        }
    }
}