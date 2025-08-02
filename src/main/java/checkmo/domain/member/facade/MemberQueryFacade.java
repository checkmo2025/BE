package checkmo.domain.member.facade;

import checkmo.domain.member.entity.Member;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import checkmo.global.dto.MemberSharedDTO;

import java.util.List;
import java.util.Map;

/**
 * Member Domain Query Facade
 * Member 도메인의 Query(조회) 관련 서비스들을 통합적으로 제공하는 Facade
 */
public interface MemberQueryFacade {

    //== MemberQueryService ==//

    /**
     * 닉네임 중복 확인 (내부용)
     *
     * @param nickname 확인할 닉네임
     * @return 중복 여부 (true: 중복됨, false: 사용 가능)
     */
    boolean isNicknameDuplicated(String nickname);

    /**
     * 회원 기본 정보 조회 (내부용)
     *
     * @param memberId 회원 ID
     * @return 회원 기본 정보 DTO
     */
    MemberResponseDTO.MemberProfileResponseDTO getMemberBasicInfo(String memberId);

    /**
     * 다른 사람 프로필 조회 (내부용)
     *
     * @param targetMemberNickname 조회 대상 회원 닉네임
     * @param memberId             조회하는 회원 ID (팔로우 여부 확인용)
     * @return targetMember의 프로필 정보 DTO
     */
    MemberResponseDTO.otherProfileResponseDTO getOtherProfile(String targetMemberNickname, String memberId);

    //== MemberFollowQueryService ==//

    /**
     * 특정 회원의 팔로워 목록 전체 조회 (내부용)
     *
     * @param memberId 조회할 회원의 ID
     * @param cursorId 커서 ID
     * @return 팔로워 목록
     */
    MemberResponseDTO.FollowList getFollowerList(String memberId, Long cursorId);

    /**
     * 특정 회원의 팔로잉 목록 전체 조회 (내부용)
     *
     * @param memberId 조회할 회원의 ID
     * @param cursorId 커서 ID
     * @return 팔로잉 목록
     */
    MemberResponseDTO.FollowList getFollowingList(String memberId, Long cursorId);

    /**
     * 특정 회원의 팔로워 목록 size 개수만큼 조회 (내부용)
     *
     * @param memberId 조회할 회원의 ID
     * @param size     조회할 개수
     * @return 팔로워 목록
     */
    MemberResponseDTO.FollowList getFollowers(Long memberId, int size);

    /**
     * 특정 회원의 팔로잉 목록 size 개수만큼 조회 (내부용)
     *
     * @param memberId 조회할 회원의 ID
     * @param size     조회할 개수
     * @return 팔로잉 목록
     */
    MemberResponseDTO.FollowList getFollowings(Long memberId, int size);

    /**
     * 닉네임으로 회원 ID 조회 (외부용)
     *
     * @param nickname 닉네임
     * @return 회원 ID
     */
    String getMemberIdByNickname(String nickname);

    /**
     * 특정 회원의 팔로우 여부 확인 (외부용)
     *
     * @param memberId             조회하는 회원 ID
     * @param targetMemberNickname 조회 대상 회원 닉네임
     * @return 팔로우 여부
     */
    boolean isFollowing(String memberId, String targetMemberNickname);

    /**
     * 공유용 기본 회원 정보 조회 (외부용)
     * @param memberId 조회할 회원 ID
     * @return MemberSharedDTO.BasicInfo
     */
    MemberSharedDTO.BasicInfoDTO getMemberBasicInfoForShare(String memberId);

    /**
     * 팔로우 상태를 포함한 공유용 회원 정보 조회 (외부용)
     * @param targetMemberId 조회 대상 회원 ID
     * @param currentMemberId 현재 로그인한 회원 ID
     * @return MemberSharedDTO.WithFollowStatus
     */
    MemberSharedDTO.WithFollowStatusDTO getMemberWithFollowStatusForShare(String targetMemberId, String currentMemberId);

    /**
     * 다른 도메인에서 관계 설정을 위해 엔티티의 프록시(참조)를 조회합니다. (외부용)
     * ‼️ 이 메소드는 실제 DB 조회를 발생시키지 않는 메소드!!!
     * ‼️ 그리고 반드시 외래 키를 설정하는 용도로만 사용되어야 함!
     *
     * 이 메소드는 구현할 때 단순히
     * {@code return memberRepository.getReferenceById(memberId);}만 하면 됨
     *
     * @param memberId 참조할 회원의 ID
     * @return Member 엔티티의 프록시 객체
     */
    Member findMemberReferenceById(String memberId);

    /**
     * 회원 ID로 회원의 닉네임을 조회합니다. (외부용)
     * @return 회원의 닉네임
     */
    String getMemberNicknameById(String memberId);

    /**
     * 회원 ID 목록으로 회원의 닉네임을 조회합니다. (외부용)
     * @param memberIds
     * @return 회원 ID와 닉네임의 매핑 정보
     */
    Map<String, String> getMemberNicknamesByMemberIds(List<String> memberIds);
}