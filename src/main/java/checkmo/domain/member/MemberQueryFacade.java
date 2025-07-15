package checkmo.domain.member;

import checkmo.domain.member.dto.MemberResponseDTO;
import checkmo.global.dto.MemberSharedDTO;

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
    MemberResponseDTO.FollowerListResponseDTO getFollowers(String memberId, Long cursorId);

    /**
     * 특정 회원의 팔로잉 목록 전체 조회 (내부용)
     *
     * @param memberId 조회할 회원의 ID
     * @param cursorId 커서 ID
     * @return 팔로잉 목록
     */
    MemberResponseDTO.FollowingListResponseDTO getFollowing(String memberId, Long cursorId);

    /**
     * 특정 회원의 팔로워 목록 size 개수만큼 조회 (내부용)
     *
     * @param memberId 조회할 회원의 ID
     * @param size     조회할 개수
     * @return 팔로워 목록
     */
    MemberResponseDTO.FollowerListResponseDTO getFollowers(Long memberId, int size);

    /**
     * 특정 회원의 팔로잉 목록 size 개수만큼 조회 (내부용)
     *
     * @param memberId 조회할 회원의 ID
     * @param size     조회할 개수
     * @return 팔로잉 목록
     */
    MemberResponseDTO.FollowingListResponseDTO getFollowing(Long memberId, int size);

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
}