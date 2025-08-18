package checkmo.domain.category.service.command;

import checkmo.apiPayload.exception.GeneralException;
import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.domain.category.entity.Category;
import checkmo.domain.category.entity.ClubCategory;
import checkmo.domain.category.entity.MemberCategory;
import checkmo.domain.category.repository.CategoryRepository;
import checkmo.domain.category.repository.ClubCategoryRepository;
import checkmo.domain.category.repository.MemberCategoryRepository;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.facade.ClubQueryFacade;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.facade.MemberQueryFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryAssignmentCommandServiceImpl implements CategoryAssignmentCommandService {

    private final ClubCategoryRepository clubCategoryRepository;
    private final MemberCategoryRepository memberCategoryRepository;
    private final ClubQueryFacade clubQueryFacade;
    private final CategoryRepository categoryRepository;
    private final MemberQueryFacade memberQueryFacade;

    @Override
    public void modifyMemberCategories(String memberId, List<Long> categoryIds) {

        // 1. 기존 카테고리 ID 리스트
        List<MemberCategory> existingMemberCategories = memberCategoryRepository.findByMemberId(memberId);

        // 2. 기존 카테고리 ID 리스트
        List<Long> existingCategoryIds = existingMemberCategories.stream()
                .map(mc -> mc.getCategory().getId())
                .toList();

        // 3. 추가할 카테고리
        List<Long> categoriesToAdd = categoryIds.stream()
                .filter(id -> !existingCategoryIds.contains(id))
                .toList();

        // 4. 제거할 카테고리
        List<Long> categoriesToRemove = existingCategoryIds.stream()
                .filter(id -> !categoryIds.contains(id))
                .toList();

        // 5. 추가
        for (Long categoryId : categoriesToAdd) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.CATEGORY_NOT_FOUND));

            Member memberProxy = memberQueryFacade.findMemberReferenceById(memberId);

            MemberCategory newMemberCategory = MemberCategory.builder()
                    .member(memberProxy)
                    .category(category)
                    .build();

            memberCategoryRepository.save(newMemberCategory);
        }

        // 6. 제거
        categoriesToRemove.forEach(categoryId -> existingMemberCategories.stream()
                                                                     .filter(mc -> mc.getCategory().getId().equals(categoryId))
                                                                     .findFirst()
                                                                     .ifPresent(memberCategoryRepository::delete));
    }

    /**
     * Club의 관심 카테고리 수정 - 이미 추가되어있는 관심 카테고리는 서비스 로직 구현 시 제외하고 새로운 카테고리만 추가 or 제거
     *
     * @param clubId      모임 ID
     * @param categoryIds 수정할 카테고리 ID 목록
     * @return 수정된 클럽 카테고리 엔티티 리스트
     */
    @Override
    public List<ClubCategory> modifyClubCategories(Long clubId, List<Long> categoryIds) {

        // 1. 기존 ClubCategory 목록 조회
        List<ClubCategory> existingClubCategories = clubCategoryRepository.findByClubId(clubId);

        // 2. 기존 카테고리 ID 리스트
        List<Long> existingCategoryIds = existingClubCategories.stream()
                .map(cc -> cc.getCategory().getId())
                .toList();

        // 3. 추가할 카테고리
        List<Long> categoriesToAdd = categoryIds.stream()
                .filter(id -> !existingCategoryIds.contains(id))
                .toList();

        // 4. 제거할 카테고리
        List<Long> categoriesToRemove = existingCategoryIds.stream()
                .filter(id -> !categoryIds.contains(id))
                .toList();

        // 5. 추가
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

        // 6. 제거
        categoriesToRemove.forEach(categoryId -> {
            existingClubCategories.stream()
                    .filter(cc -> cc.getCategory().getId().equals(categoryId))
                    .findFirst()
                    .ifPresent(clubCategoryRepository::delete);
        });

        // 7. 최종 카테고리 목록 (순수 엔티티) 반환
        return clubCategoryRepository.findByClubId(clubId);
    }
}
