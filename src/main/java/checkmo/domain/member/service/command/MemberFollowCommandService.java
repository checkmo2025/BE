package checkmo.domain.member.service.command;

/**
 * 팔로우/팔로잉 처리하는 서비스
 *
 * 팔로우/언팔로우 기능을 담당
 * 팔로우/팔로잉 시 NotificationCommandService를 통해 알림 처리
 */
public interface MemberFollowCommandService {

    /**
     * 특정 회원을 팔로우
     *
     * @param memberId 팔로우할 회원의 ID
     * @param targetNickname 팔로우 대상 회원의 nickname -> 서비스 로직에서 닉네임으로 회원의 ID를 조회하여 팔로우 처리
     */
    void followMember(String memberId, String targetNickname);

    /**
     * 특정 회원의 팔로우를 취소 (언팔로우)
     *
     * @param memberId 언팔로우할 회원의 ID
     * @param targetNickname 팔로우 대상 회원의 nickname -> 서비스 로직에서 닉네임으로 회원의 ID를 조회하여 팔로우 처리
     */
    void unfollowMember(String memberId, String targetNickname);

    /**
     * 특정 회원의 팔로잉를 취소 (언팔로잉)
     *
     * @param memberId 언팔로우할 회원의 ID
     * @param targetNickname 팔로우 대상 회원의 nickname -> 서비스 로직에서 닉네임으로 회원의 ID를 조회하여 팔로우 처리
     */
    void unfollowingMember(String memberId, String targetNickname);
}
