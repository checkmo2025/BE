package checkmo.domain.category.facade.impl;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.category.entity.Category;
import checkmo.domain.category.entity.ClubCategory;
import checkmo.domain.category.facade.CategoryCommandFacade;
import checkmo.domain.category.repository.CategoryRepository;
import checkmo.domain.category.repository.ClubCategoryRepository;
import checkmo.domain.category.web.dto.CategoryRequestDTO;
import checkmo.domain.club.entity.Club;
import checkmo.global.dto.CategorySharedDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryCommandFacadeImpl implements CategoryCommandFacade {

    private final CategoryRepository categoryRepository;
    private final ClubCategoryRepository clubCategoryRepository;

    @Override
    public CategorySharedDTO.CategoryInfoListDTO modifyMemberCategories(Long memberId, CategoryRequestDTO.CategoryListRequestDTO request) {
        return null;
    }

    @Override
    public CategorySharedDTO.CategoryInfoListDTO modifyClubCategories(
            Long clubId,
            CategoryRequestDTO.CategoryListRequestDTO request
    ) {
        // 1. 기존 ClubCategory 전부 삭제
        clubCategoryRepository.deleteById(clubId);

        // 2. 요청받은 categoryIds로 Category 조회
        List<Long> categoryIds = request.getCategoryId();
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new GeneralException(ErrorStatus.CATEGORY_NOT_FOUND);
        }

        // 3. ClubCategory 새로 생성 및 저장
        List<ClubCategory> newClubCategories = categories.stream()
                .map(category -> ClubCategory.builder()
                        .club(Club.builder().id(clubId).build()) // clubId만 가진 더미 객체 사용
                        .category(category)
                        .build())
                .toList();
        clubCategoryRepository.saveAll(newClubCategories);

        // 4. 응답 DTO
        List<CategorySharedDTO.CategoryInfoDTO> resultList = categories.stream()
                .map(category -> CategorySharedDTO.CategoryInfoDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .toList();

        return CategorySharedDTO.CategoryInfoListDTO.builder()
                .categoryList(resultList)
                .build();
    }

}
