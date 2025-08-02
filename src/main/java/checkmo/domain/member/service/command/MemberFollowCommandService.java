package checkmo.domain.member.service.command;

/**
 * 팔로잉 처리하는 서비스
 *
 * 팔로잉/언팔로우 기능을 담당
 * 팔로잉 NotificationCommandService를 통해 알림 처리
 */
public interface MemberFollowCommandService {

    /**
     * 특정 회원을 팔로잉
     *
     * @param memberId 팔로우할 회원의 ID
     * @param followingNickname 팔로우 대상 회원의 nickname -> 서비스 로직에서 닉네임으로 회원의 ID를 조회하여 팔로잉 처리
     */
    void followingMember(String memberId, String followingNickname);

    /**
     * 특정 회원의 팔로잉를 취소 (언팔로잉)
     *
     * @param memberId 언팔로잉할 회원의 ID
     * @param followingNickname 팔로우 대상 회원의 nickname -> 서비스 로직에서 닉네임으로 회원의 ID를 조회하여 언팔로잉 처리
     */
    void unfollowingMember(String memberId, String followingNickname);

    /**
     * 내 팔로워 중 특정 회원 삭제
     *
     * @param memberId 제거할 회원 ID
     * @param followingNickname 팔로워의 nickname -> 서비스 로직에서 닉네임으로 회원의 ID를 조회하여 팔로워 삭제 처리
     */
    void deleteFollower(String memberId, String followingNickname);
}
