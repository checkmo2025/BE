package checkmo.domain.category.facade.impl;

import checkmo.domain.category.facade.CategoryCommandFacade;
import checkmo.domain.category.web.dto.CategoryRequestDTO;
import checkmo.global.dto.CategorySharedDTO;
import org.springframework.stereotype.Service;

@Service
public class CategoryCommandFacadeImpl implements CategoryCommandFacade {

    @Override
    public CategorySharedDTO.CategoryInfoListDTO modifyMemberCategories(Long memberId, CategoryRequestDTO.CategoryListRequestDTO request) {
        return null;
    }

    @Override
    public CategorySharedDTO.CategoryInfoListDTO modifyClubCategories(Long clubId, CategoryRequestDTO.CategoryListRequestDTO request) {
        return null;
    }
}
