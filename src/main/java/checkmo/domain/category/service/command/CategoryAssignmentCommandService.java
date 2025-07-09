package checkmo.domain.category.service.command;

import java.util.List;

/**
 * 회원-카테고리, 모임-카테고리 간의 관계 설정을 처리
 */
public interface CategoryAssignmentCommandService {
    /**
     * 회원의 관심 카테고리 설정
     *
     * @param memberId     회원 ID
     * @param categoryIds  추가할 카테고리 ID 목록
     */
    void addCategoriesToMember(Long memberId, List<Long> categoryIds);

    /**
     * 회원의 관심 카테고리 제거
     *
     * @param memberId     회원 ID
     * @param categoryIds  제거할 카테고리 ID 목록
     */
    void removeCategoriesFromMember(Long memberId, List<Long> categoryIds);

    /**
     * Club의 관심 카테고리 설정
     *
     * @param clubId       모임 ID
     * @param categoryIds  추가할 카테고리 ID 목록
     */
    void addCategoriesToClub(Long clubId, List<Long> categoryIds);

    /**
     * Club의 관심 카테고리 제거
     *
     * @param clubId       모임 ID
     * @param categoryIds  제거할 카테고리 ID 목록
     */
    void removeCategoriesFromClub(Long clubId, List<Long> categoryIds);
}
