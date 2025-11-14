package checkmo.clubManagement;

import java.util.List;

public interface ClubManagementAPI {

    /**
     * 특정 회원이 가입한 모임 목록을 조회합니다. (외부용) 마이페이지 등 다른 서비스에서 사용됩니다.
     *
     * @param memberId 회원 ID
     * @return 회원이 가입한 모임의 간략한 정보 목록 DTO
     */
    ClubManagementExternalDTO.MyClubList getMyClubListForShare(String memberId);

    /**
     * 특정 회원이 해당 클럽의 멤버인지 확인합니다.
     *
     * @param memberId 회원 ID
     * @param clubId   클럽 ID
     * @return 클럽 멤버 여부 (MEMBER 또는 STAFF 상태인 경우 true)
     */
    boolean isMemberInClub(String memberId, Long clubId);

    /**
     * 특정 클럽에 속한 회원 ID 목록을 조회합니다.
     *
     * @param clubId 클럽 ID
     * @return 클럽에 속한 회원 ID 목록 (MEMBER 또는 STAFF 상태인 회원만)
     */
    List<String> getClubMemberIds(Long clubId);

}
