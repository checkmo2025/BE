package checkmo.member.internal.service.query;

import checkmo.member.entity.MemberCategory;

import java.util.List;

/**
 * 화원의 카테고리 조회를 처리하는 서비스
 */
public interface MemberCategoryQueryService {
    /**
     * 회원의 관심 카테고리 목록 조회
     *
     * @param memberId 회원 ID
     * @return 회원의 카테고리 리스트
     */
    List<MemberCategory> findCategoriesByMember(String memberId);
}