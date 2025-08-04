package checkmo.domain.member.service.query;

import checkmo.domain.member.entity.Follow;

import java.util.List;

/**
 * 팔로우/팔로잉 조회 서비스
 *
 * 팔로워/팔로잉 목록 조회
 */
public interface MemberFollowQueryService {

    /**
     * 특정 회원의 팔로우 목록 전체 조회
     *
     * @param memberId 조회할 회원의 ID
     * @param cursorId 커서 ID (페이징을 위한) => 커서로 사용되는 ID는 Follow 엔티티 자체의 ID 값으로 사용하기!!
     * @return 팔로워 목록
     */
    List<Follow> getFollowerList(String memberId, Long cursorId, int pageSize);

    /**
     * 특정 회원의 팔로잉 목록 전체 조회
     *
     * @param memberId 조회할 회원의 ID
     * @param cursorId 커서 ID (페이징을 위한) => 커서로 사용되는 ID는 Follow 엔티티 자체의 ID 값으로 사용하기!!
     * @return 팔로잉 목록
     */
    List<Follow> getFollowingList(String memberId, Long cursorId, int pageSize);

    /**
     * 특정 회원의 팔로우 목록 size 개수만큼 조회
     *
     * @param memberId 조회할 회원의 ID
     * @return 팔로워 목록
     */
    List<Follow> getFollowers(String memberId, int size);

    /**
     * 특정 회원의 팔로잉 목록 size 개수만큼 조회
     *
     * @param memberId 조회할 회원의 ID
     * @return 팔로잉 목록
     */
    List<Follow> getFollowings(String memberId, int size);

    /**
     * 특정 회원의 팔로우 여부 확인
     */
    boolean isFollowing(String memberId, String targetMemberId);
}
