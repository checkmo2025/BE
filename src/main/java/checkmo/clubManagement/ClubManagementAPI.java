package checkmo.clubManagement;

import checkmo.clubManagement.ClubManagementExternalDTO.Membership;
import checkmo.common.apiPayload.exception.GeneralException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ClubManagementAPI {

    /**
     * 특정 모임이 존재하는지 검증. (외부용)
     *
     * @param clubId 모임 ID
     */
    void validateClub(Long clubId) throws GeneralException;

    /**
     * 특정 회원이 가입한 모임 목록을 조회합니다. (외부용) 마이페이지 등 다른 서비스에서 사용됩니다.
     *
     * @param memberId 회원 ID
     * @return 회원이 가입한 모임의 간략한 정보 목록 DTO
     */
    ClubManagementExternalDTO.MyClubList getMyClubListForShare(String memberId);

    /**
     * 특정 클럽에 속한 회원 ID 목록을 조회합니다.
     *
     * @param clubId 클럽 ID
     * @return 클럽에 속한 회원 ID 목록 (MEMBER 또는 STAFF 상태인 회원만)
     */
    List<String> getClubMemberIds(Long clubId);

    /**
     * 특정 회원이 해당 클럽의 멤버인지 확인합니다.
     *
     * @param memberId 회원 ID
     * @param clubId   클럽 ID
     * @return 클럽 멤버 여부 (MEMBER 또는 STAFF 상태인 경우 true)
     */
    boolean isMemberInClub(String memberId, Long clubId);

    /**
     * 특정 모임의 특정 회원이 STAFF 상태인지 검증 (외부용)
     *
     * @param clubId   모임 ID
     * @param memberId 회원 ID
     * @return ClubMemberId
     */
    Long getStaffClubMemberInfo(Long clubId, String memberId) throws GeneralException;

    /**
     * 특정 모임의 특정 회원이 ACTIVE 상태인지 검증 (외부용)
     *
     * @param clubId   모임 ID
     * @param memberId 회원 ID
     * @return ClubMemberId
     */
    Long getActiveClubMemberInfo(Long clubId, String memberId) throws GeneralException;

    /**
     * 특정 모임의 특정 회원의 멤버십 정보를 조회 (외부용)
     *
     * @param clubId   모임 ID
     * @param memberId 회원 ID
     * @return MembershipDTO
     */
    Membership getClubMembershipInfo(Long clubId, String memberId) throws GeneralException;

    /**
     * 특정 모임의 멤버십 정보를 조회 (외부용)
     */
    Map<Long, Membership> getClubMembershipInfos(Set<Long> clubMemberIds) throws GeneralException;

    /**
     * 특정 클럽에 속한 ACTIVE한 회원 목록을 커서 기반 조회합니다.
     *
     * @param clubId   클럽 ID
     * @param cursorId 마지막으로 조회된 멤버십 ID (처음 조회 시 null)
     * @param size     한 번에 조회할 멤버십 수
     * @return 멤버십 목록
     */
    List<Membership> getClubMembersByStatus(Long clubId, Long cursorId, Integer size);
}
