package checkmo.clubManagement;

import static checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;

import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ClubManagementAPI {

    /**
     * 특정 모임이 존재하는지 검증.
     *
     * @param clubId 모임 ID
     */
    void validateClub(Long clubId) throws ClubManagementException;

    /**
     * 특정 모임의 이름을 조회.
     *
     * @param clubId 모임 ID
     * @return 모임 이름
     */
    String fetchClubName(Long clubId) throws ClubManagementException;

    /**
     * 여러 모임의 이름을 배치 조회.
     *
     * @param clubIds 모임 ID 목록
     * @return 모임 ID를 키로 하는 모임 이름 맵
     */
    Map<Long, String> fetchClubNamesByClubIds(List<Long> clubIds);

    /**
     * 특정 클럽에 속한 ACTIVE한 회원 ID 목록을 조회합니다.
     *
     * @param clubId 클럽 ID
     * @return 클럽에 속한 회원 ID 목록 (MEMBER 또는 STAFF 상태인 회원만)
     */
    List<String> fetchActiveMemberIds(Long clubId);

    /**
     * 특정 모임의 특정 회원이 STAFF 상태인지 검증
     *
     * @param clubId   모임 ID
     * @param memberId 회원 ID
     * @throws ClubManagementException STAFF 상태가 아닐 경우 예외 발생
     */
    void validateStaffClubMember(Long clubId, String memberId) throws ClubManagementException;

    /**
     * 특정 모임의 특정 회원들이 club에 속하고, ACTIVE 상태인지 검증
     *
     * @param clubId        모임 ID
     * @param clubMemberIds 모임 멤버십 ID 집합 (중복 X)
     * @throws ClubManagementException 클럽에 속하지 않거나, ACTIVE 상태가 아닌 멤버가 있을 경우 예외 발생
     */
    void validateActiveClubMembers(Long clubId, Set<Long> clubMemberIds) throws ClubManagementException;

    /**
     * 특정 모임의 특정 회원이 ACTIVE 상태인지 검증
     *
     * @param clubId   모임 ID
     * @param memberId 회원 ID
     * @return ClubMemberId
     */
    Long fetchActiveClubMemberId(Long clubId, String memberId) throws ClubManagementException;

    /**
     * 특정 모임의 특정 회원의 멤버십 정보를 조회
     *
     * @param clubId   모임 ID
     * @param memberId 회원 ID
     * @return MembershipDTO
     */
    MembershipInfo fetchMembershipInfo(Long clubId, String memberId)
            throws ClubManagementException;

    /**
     * 특정 모임의 멤버십 정보를 배치 조회
     *
     * @param clubMemberIds 모임 멤버십 ID 집합 (중복 X)
     * @return 멤버십 ID를 키로 하는 멤버십 정보
     */
    Map<Long, MembershipInfo> fetchMembershipInfoByClubMemberIds(Set<Long> clubMemberIds)
            throws ClubManagementException;

    /**
     * 특정 클럽에 속한 ACTIVE한 회원 목록을 전체 조회합니다.
     *
     * @param clubId 클럽 ID
     * @return 멤버십 목록
     */
    List<MembershipInfo> fetchAllActiveMembershipInfo(Long clubId);

    /**
     * 특정 모임의 마지막 활동 시간을 갱신합니다.
     *
     * @param clubId           모임 ID
     * @param lastActivityTime 마지막 활동 시간
     */
    void touchLastActivity(Long clubId, LocalDateTime lastActivityTime);
}
