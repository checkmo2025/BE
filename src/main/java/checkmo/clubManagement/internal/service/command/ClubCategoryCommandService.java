package checkmo.clubManagement.internal.service.command;

import checkmo.clubManagement.entity.Club;
import java.util.List;

/**
 * 클럽의 카테고리 관련 작업(생성/수정/삭제)을 처리하는 서비스
 */
public interface ClubCategoryCommandService {

    /**
     * 클럽의 카테고리 연관관계를 생성합니다.
     *
     * @param club        클럽
     * @param categoryIds 카테고리 ID 목록
     */
    void createClubCategories(Club club, List<Long> categoryIds);

    /**
     * 클럽의 카테고리 연관관계를 수정합니다.
     *
     * @param club        클럽
     * @param categoryIds 새로운 카테고리 ID 목록
     */
    void modifyClubCategories(Club club, List<Long> categoryIds);
}