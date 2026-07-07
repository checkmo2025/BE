package checkmo.member;

import checkmo.member.MemberExternalDTO.BasicInfoWithFollow;
import checkmo.member.MemberExternalDTO.InterestCategoryInfo;

import java.util.List;
import java.util.Map;

public interface MemberAPI {

    /**
     * 닉네임으로 회원 ID 조회
     *
     * @param nickname 닉네임
     * @return 회원 ID
     */
    Long fetchMemberId(String nickname);

    /**
     * 회원 ID로 회원의 닉네임을 조회합니다.
     *
     * @return 회원의 닉네임
     */
    String fetchNickname(Long memberId);

    /**
     * 회원 ID 목록으로 회원의 닉네임을 조회합니다.
     *
     * @return 회원 ID와 닉네임의 매핑 정보
     */
    Map<Long, String> fetchNicknameByMemberIds(List<Long> memberIds);

    /**
     * 공유용 기본 회원 정보 조회
     *
     * @param memberId 조회할 회원 ID
     * @return MemberExternalDTO.BasicInfo
     */
    MemberExternalDTO.BasicInfo fetchMemberBasicInfo(Long memberId);

    /**
     * 회원 ID 목록으로 공유용 기본 회원 정보 조회
     *
     * @param memberIds 조회할 회원 ID 목록
     * @return 회원 ID와 기본 정보 매핑 리스트
     */
    Map<Long, MemberExternalDTO.BasicInfo> fetchMemberBasicInfoByMemberIds(List<Long> memberIds);

    /**
     * 회원 ID 목록으로 공유용 디테일 회원 정보 조회
     *
     * @param memberIds 조회할 회원 ID 목록
     * @return 회원 ID와 디테일 정보 매핑 리스트
     */
    Map<Long, MemberExternalDTO.DetailInfo> fetchMemberDetailInfoByMemberIds(List<Long> memberIds);

    /**
     * 회원 ID 목록으로 공유용 개인 정보 조회
     *
     * @param memberIds 조회할 회원 ID 목록
     * @return 회원 ID와 개인 정보 매핑 리스트
     */
    Map<Long, MemberExternalDTO.PersonalInfo> fetchMemberPersonalInfoByMemberIds(List<Long> memberIds);

    /**
     * 팔로우 상태를 포함한 공유용 회원 정보 조회
     *
     * @param targetMemberId  조회 대상 회원 ID
     * @param currentMemberId 현재 로그인한 회원 ID
     * @return MemberExternalDTO.WithFollowStatus
     */
    BasicInfoWithFollow fetchMemberBasicInfoWithFollow(Long targetMemberId, Long currentMemberId);

    /**
     * 회원 ID 목록으로 팔로우 상태를 포함한 공유용 회원 정보를 조회합니다.
     *
     * @param targetMemberIds 조회 대상 회원 ID 목록
     * @param currentMemberId 현재 로그인한 회원 ID
     * @return 회원 ID와 팔로우 상태 포함 정보 매핑
     */
    Map<Long, BasicInfoWithFollow> fetchMemberBasicInfoWithFollowByMemberId(
            List<Long> targetMemberIds,
            Long currentMemberId
    );

    /**
     * 특정 회원이 팔로우하는 회원 ID 목록을 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 팔로우하는 회원 ID 목록
     */
    List<Long> fetchFollowingIds(Long memberId);

    /**
     * 특정 회원이 차단한 회원 ID 목록을 조회합니다.
     *
     * @param blockerId 차단 주체 회원 ID
     * @return 차단당한 회원 ID 목록
     */
    List<Long> fetchBlockedMemberIds(Long blockerId);

    /**
     * 특정 회원과 차단 관계가 있는 모든 회원 ID 목록을 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 내가 차단했거나 나를 차단한 회원 ID 목록
     */
    List<Long> fetchBlockRelatedMemberIds(Long memberId);

    /**
     * 두 회원 사이에 어느 방향이든 차단 관계가 있는지 조회합니다.
     *
     * @param memberId1 회원 ID
     * @param memberId2 회원 ID
     * @return 차단 관계 존재 여부
     */
    boolean hasBlockBetween(Long memberId1, Long memberId2);

    /**
     * 차단 주체가 특정 회원을 차단했는지 조회합니다.
     *
     * @param blockerId 차단 주체 회원 ID
     * @param blockedId 피차단 회원 ID
     * @return 차단 여부
     */
    boolean hasBlocked(Long blockerId, Long blockedId);

    /**
     * 공개 프로필/서재/책이야기 직접 조회 가능 여부를 검증합니다.
     *
     * @param viewerId 조회하는 회원 ID
     * @param targetMemberId 조회 대상 회원 ID
     */
    void validateProfileAccessible(Long viewerId, Long targetMemberId);

    /**
     * 회원의 관심 카테고리 정보를 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 관심 카테고리 정보
     */
    InterestCategoryInfo fetchInterestCategory(Long memberId);

    /**
     * 회원 ID로 회원의 이메일을 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 회원 이메일
     */
    String fetchMemberEmail(Long memberId);

    /**
     * 회원 ID 목록으로 회원의 이메일을 조회합니다. 비활성 회원도 포함하며, 삭제된 회원은 결과에서 제외합니다.
     *
     * @param memberIds 회원 ID 목록
     * @return 회원 ID와 이메일의 매핑 정보
     */
    Map<Long, String> fetchMemberEmailByMemberIdsIncludingDeactivated(List<Long> memberIds);
}
