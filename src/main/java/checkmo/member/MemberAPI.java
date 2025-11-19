package checkmo.member;

import java.util.List;
import java.util.Map;

/**
 * Member Domain Query Facade Member 도메인의 Query(조회) 관련 서비스들을 통합적으로 제공하는 Facade
 */
public interface MemberAPI {

    /**
     * 닉네임으로 회원 ID 조회
     *
     * @param nickname 닉네임
     * @return 회원 ID
     */
    String getMemberIdByNickname(String nickname);

    /**
     * 닉네임 목록으로 회원 ID 조회
     *
     * @param nicknames 닉네임 목록
     * @return 닉네임과 회원 ID 매핑 정보
     */
    Map<String, String> getMemberIdsByNicknames(List<String> nicknames);

    /**
     * 회원 ID로 회원의 닉네임을 조회합니다.
     *
     * @return 회원의 닉네임
     */
    String getMemberNicknameById(String memberId);

    /**
     * 회원 ID 목록으로 회원의 닉네임을 조회합니다.
     *
     * @return 회원 ID와 닉네임의 매핑 정보
     */
    Map<String, String> getMemberNicknamesByMemberIds(List<String> memberIds);

    /**
     * 공유용 기본 회원 정보 조회
     *
     * @param memberId 조회할 회원 ID
     * @return MemberExternalDTO.BasicInfo
     */
    MemberExternalDTO.BasicInfo getMemberBasicInfoForShare(String memberId);

    /**
     * 회원 ID 목록으로 공유용 기본 회원 정보 조회
     *
     * @param memberIds 조회할 회원 ID 목록
     * @return 회원 ID와 기본 정보 매핑 리스트
     */
    Map<String, MemberExternalDTO.BasicInfo> getMemberBasicInfoMapForShare(List<String> memberIds);

    /**
     * 팔로우 상태를 포함한 공유용 회원 정보 조회
     *
     * @param targetMemberId  조회 대상 회원 ID
     * @param currentMemberId 현재 로그인한 회원 ID
     * @return MemberExternalDTO.WithFollowStatus
     */
    MemberExternalDTO.WithFollowStatus getMemberWithFollowStatusForShare(String targetMemberId, String currentMemberId);

    /**
     * 회원 ID 목록으로 팔로우 상태를 포함한 공유용 회원 정보를 조회합니다.
     *
     * @param targetMemberIds 조회 대상 회원 ID 목록
     * @param currentMemberId 현재 로그인한 회원 ID
     * @return 회원 ID와 팔로우 상태 포함 정보 매핑
     */
    Map<String, MemberExternalDTO.WithFollowStatus> getMemberWithFollowStatusMapForShare(
            List<String> targetMemberIds,
            String currentMemberId
    );

    /**
     * 특정 회원이 팔로우하는 회원 ID 목록을 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 팔로우하는 회원 ID 목록
     */
    List<String> getFollowingMemberIds(String memberId);
}