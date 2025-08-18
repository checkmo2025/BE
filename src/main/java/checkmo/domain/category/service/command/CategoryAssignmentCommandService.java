package checkmo.domain.category.service.command;

import checkmo.domain.category.entity.ClubCategory;

import java.util.List;

/**
 * 회원-카테고리, 모임-카테고리 간의 관계 설정을 처리
 */
public interface CategoryAssignmentCommandService {
    /**
     * 회원의 관심 카테고리 수정 - 이미 추가되어있는 관심 카테고리는 서비스 로직 구현 시 제외하고 새로운 카테고리만 추가 or 제거
     *
     * @param memberId    회원 ID
     * @param categoryIds 수정할 카테고리 ID 목록
     */
    void modifyMemberCategories(String memberId, List<Long> categoryIds);

    /**
     * Club의 관심 카테고리 수정 - 이미 추가되어있는 관심 카테고리는 서비스 로직 구현 시 제외하고 새로운 카테고리만 추가 or 제거
     *
     * @param clubId      모임 ID
     * @param categoryIds 수정할 카테고리 ID 목록
     * @return 수정된 클럽 카테고리 엔티티 리스트
     */
    List<ClubCategory> modifyClubCategories(Long clubId, List<Long> categoryIds);
}
