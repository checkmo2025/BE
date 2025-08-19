package checkmo.domain.club.service.command;

import java.util.List;

/**
 * 클럽의 카테고리 관련 작업(생성/수정/삭제)을 처리하는 서비스
 */
public interface ClubCategoryCommandService {

    /**
     * 클럽의 카테고리 연관관계를 수정합니다.
     *
     * @param clubId      클럽 ID
     * @param categoryIds 새로운 카테고리 ID 목록
     */
    void modifyClubCategories(Long clubId, List<Long> categoryIds);
}