package checkmo.member;

import checkmo.member.internal.entity.Member;
import java.util.List;
import java.util.Map;

/**
 * Member Domain Query Facade Member 도메인의 Query(조회) 관련 서비스들을 통합적으로 제공하는 Facade
 */
public interface MemberAPI {

    /**
     * 닉네임으로 회원 ID 조회 (외부용)
     *
     * @param nickname 닉네임
     * @return 회원 ID
     */
    String getMemberIdByNickname(String nickname);

    /**
     * 닉네임 목록으로 회원 ID 조회 (외부용)
     *
     * @param nicknames 닉네임 목록
     * @return 닉네임과 회원 ID 매핑 정보
     */
    Map<String, String> getMemberIdsByNicknames(List<String> nicknames);

    /**
     * 공유용 기본 회원 정보 조회 (외부용)
     *
     * @param memberId 조회할 회원 ID
     * @return MemberExternalDTO.BasicInfo
     */
    MemberExternalDTO.BasicInfo getMemberBasicInfoForShare(String memberId);

    /**
     * 회원 ID 목록으로 공유용 기본 회원 정보 조회 (외부용)
     *
     * @param memberIds 조회할 회원 ID 목록
     * @return 회원 ID와 기본 정보 매핑 리스트
     */
    Map<String, MemberExternalDTO.BasicInfo> getMemberBasicInfoMapForShare(List<String> memberIds);

    /**
     * 팔로우 상태를 포함한 공유용 회원 정보 조회 (외부용)
     *
     * @param targetMemberId  조회 대상 회원 ID
     * @param currentMemberId 현재 로그인한 회원 ID
     * @return MemberExternalDTO.WithFollowStatus
     */
    MemberExternalDTO.WithFollowStatus getMemberWithFollowStatusForShare(String targetMemberId, String currentMemberId);

    /**
     * 회원 ID 목록으로 팔로우 상태를 포함한 공유용 회원 정보를 조회합니다. (외부용)
     *
     * @param targetMemberIds 조회 대상 회원 ID 목록
     * @param currentMemberId 현재 로그인한 회원 ID
     * @return 회원 ID와 팔로우 상태 포함 정보 매핑
     */
    Map<String, MemberExternalDTO.WithFollowStatus> getMemberWithFollowStatusMapForShare(List<String> targetMemberIds,
                                                                                         String currentMemberId);

    /**
     * 다른 도메인에서 관계 설정을 위해 엔티티의 프록시(참조)를 조회합니다. (외부용) ‼️ 이 메소드는 실제 DB 조회를 발생시키지 않는 메소드!!! ‼️ 그리고 반드시 외래 키를 설정하는 용도로만
     * 사용되어야 함!
     * <p>
     * 이 메소드는 구현할 때 단순히 {@code return memberRepository.getReferenceById(memberId);}만 하면 됨
     *
     * @param memberId 참조할 회원의 ID
     * @return Member 엔티티의 프록시 객체
     */
    Member findMemberReferenceById(String memberId);

    /**
     * 회원 ID로 회원의 닉네임을 조회합니다. (외부용)
     *
     * @return 회원의 닉네임
     */
    String getMemberNicknameById(String memberId);

    /**
     * 회원 ID 목록으로 회원의 닉네임을 조회합니다. (외부용)
     *
     * @return 회원 ID와 닉네임의 매핑 정보
     */
    Map<String, String> getMemberNicknamesByMemberIds(List<String> memberIds);
}