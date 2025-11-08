package checkmo.member.internal.service.command;

import java.util.List;

/**
 * 회원의 카테고리 설정을 처리하는 서비스
 */
public interface MemberCategoryCommandService {
    /**
     * 회원의 관심 카테고리 수정
     *
     * @param memberId 회원 ID
     * @param categoryIds 수정할 카테고리 ID 목록
     */
    void modifyMemberCategories(String memberId, List<Long> categoryIds);
}