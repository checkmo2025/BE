package checkmo.domain.club.service.query;

/**
 * 독서클럽 회원에 대한 조회 서비스
 * <p>
 * 독서클럽 회원의 권한 확인 및 회원 존재 확인 기능을 담당합니다.
 */
public interface ClubMemberQueryService {

    /**
     * 독서클럽 회원인지 확인합니다.
     *
     * @param clubId   독서동아리 id
     * @param memberId 회원 id
     * @return true: 독서동아리 회원, false: 회원 아님
     */
    boolean isClubMember(Long clubId, String memberId);

    /**
     * 독서클럽의 특정 회원이 운영진(STAFF)인지 확인합니다.(권한 확인용)
     *
     * @param clubId
     * @param memberId
     * @return true: 운영진, false: 운영진 아님
     */
    boolean isClubStaff(Long clubId, String memberId);

}
