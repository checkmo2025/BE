package checkmo.domain.member.service.command;

import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.domain.category.entity.Category;
import checkmo.domain.category.repository.CategoryRepository;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.entity.MemberCategory;
import checkmo.domain.member.repository.MemberCategoryRepository;
import checkmo.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberCategoryCommandServiceImpl implements MemberCategoryCommandService {

    // Domain level 1의 Repository
    private final CategoryRepository categoryRepository;

    // 자신의 Repository
    private final MemberCategoryRepository memberCategoryRepository;
    private final MemberRepository memberRepository;

    @Override
    public void modifyMemberCategories(String memberId, List<Long> categoryIds) {
        // 같은 도메인 내부이므로 직접 Member 엔티티 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 1. 기존 카테고리 조회
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

            MemberCategory newMemberCategory = MemberCategory.builder()
                    .member(member)
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
}