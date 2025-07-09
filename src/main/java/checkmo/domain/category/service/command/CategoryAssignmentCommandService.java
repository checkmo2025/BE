package checkmo.domain.category.service.command;

import checkmo.domain.category.dto.CategoryRequestDTO;
import checkmo.domain.category.dto.CategoryResponseDTO;

import java.util.List;

/**
 * 회원-카테고리, 모임-카테고리 간의 관계 설정을 처리
 */
public interface CategoryAssignmentCommandService {
    /**
     * 회원의 관심 카테고리 설정
     *
     * @param memberId     회원 ID
     * @param request  추가할 카테고리 ID 목록
     * @return 추가된 카테고리 정보가 담긴 DTO
     */
    CategoryResponseDTO.CategoryListResponseDTO addCategoriesToMember(Long memberId, CategoryRequestDTO.CategoryListRequestDTO request);

    /**
     * 회원의 관심 카테고리 제거
     *
     * @param memberId     회원 ID
     * @param request  제거할 카테고리 ID 목록
     * @return 제거되고 남아있는 카테고리 정보가 담긴 DTO
     */
    CategoryResponseDTO.CategoryListResponseDTO removeCategoriesFromMember(Long memberId, CategoryRequestDTO.CategoryListRequestDTO request);

    /**
     * Club의 관심 카테고리 설정
     *
     * @param clubId       모임 ID
     * @param request  추가할 카테고리 ID 목록
     * @return 추가된 카테고리 정보가 담긴 DTO
     */
    CategoryResponseDTO.CategoryListResponseDTO addCategoriesToClub(Long clubId, CategoryRequestDTO.CategoryListRequestDTO request);

    /**
     * Club의 관심 카테고리 제거
     *
     * @param clubId       모임 ID
     * @param request  제거할 카테고리 ID 목록
     * @return 제거되고 남아있는 카테고리 정보가 담긴 DTO
     */
    CategoryResponseDTO.CategoryListResponseDTO removeCategoriesFromClub(Long clubId, CategoryRequestDTO.CategoryListRequestDTO request);
}
