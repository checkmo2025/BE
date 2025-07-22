package checkmo.domain.category.facade.impl;

import checkmo.domain.category.facade.CategoryCommandFacade;
import checkmo.domain.category.service.command.CategoryAssignmentCommandService;
import checkmo.domain.category.web.dto.CategoryRequestDTO;
import checkmo.domain.category.web.dto.CategoryResponseDTO;
import checkmo.global.dto.CategorySharedDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryCommandFacadeImpl implements CategoryCommandFacade {

    private final CategoryAssignmentCommandService categoryAssignmentCommandService;

    @Override
    public CategorySharedDTO.CategoryInfoListDTO modifyMemberCategories(Long memberId, CategoryRequestDTO.CategoryListRequestDTO request) {
        return null;
    }

    @Override
    public CategorySharedDTO.CategoryInfoListDTO modifyClubCategories(Long clubId, CategoryRequestDTO.CategoryListRequestDTO request) {
        CategoryResponseDTO.CategoryListResponseDTO responseDTO = categoryAssignmentCommandService.modifyClubCategories(clubId, request);

        List<CategorySharedDTO.CategoryInfoDTO> sharedDTOList = responseDTO.getCategoryList().stream()
                .map(dto -> CategorySharedDTO.CategoryInfoDTO.builder()
                        .id(dto.getId())
                        .name(dto.getName())
                        .build())
                .toList();

        return CategorySharedDTO.CategoryInfoListDTO.builder()
                .categoryList(sharedDTOList)
                .build();
    }

}
