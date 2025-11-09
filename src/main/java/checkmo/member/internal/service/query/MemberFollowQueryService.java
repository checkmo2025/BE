package checkmo.member.internal.service.query;

import checkmo.member.internal.entity.Follow;
import java.util.List;
import java.util.Map;

/**
 * 팔로우/팔로잉 조회 서비스
 * <p>
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

    /**
     * 특정 회원이 여러 회원들을 팔로우하는지 배치로 확인 (배치 처리용)
     *
     * @param currentMemberId 현재 회원 ID
     * @param targetMemberIds 확인할 대상 회원 ID 목록
     * @return 대상 회원 ID별 팔로우 여부 매핑
     */
    Map<String, Boolean> getFollowStatusMapForMembers(String currentMemberId, List<String> targetMemberIds);
}
