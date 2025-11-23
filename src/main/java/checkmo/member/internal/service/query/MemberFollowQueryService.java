package checkmo.member.internal.service.query;

import checkmo.member.internal.entity.Follow;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.repository.FollowRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberFollowQueryService {

    private final FollowRepository followRepository;

    /**
     * 특정 회원의 팔로워 목록 조회
     *
     * @param memberId 조회할 회원의 ID
     * @param cursorId 커서 ID
     * @param pageSize 페이지 크기
     * @return 팔로워 목록
     */
    public List<Follow> retrieveFollowers(String memberId, Long cursorId, int pageSize) {
        return followRepository.findFollowers(memberId, cursorId, pageSize);
    }

    /**
     * 특정 회원의 팔로잉 목록 조회
     *
     * @param memberId 조회할 회원의 ID
     * @param cursorId 커서 ID
     * @param pageSize 페이지 크기
     * @return 팔로잉 목록
     */
    public List<Follow> retrieveFollowingIds(String memberId, Long cursorId, int pageSize) {
        return followRepository.findFollowings(memberId, cursorId, pageSize);
    }

    /**
     * 특정 회원의 팔로우 여부 확인
     */
    public boolean isFollowing(String memberId, String targetMemberId) {
        if (Member.isSameMember(memberId, targetMemberId)) {
            return true; // 자기 자신을 팔로우하는 것은 항상 true
        }

        // 팔로우 관계가 존재하는지 확인
        return followRepository.existsByFollowerIdAndFollowingId(memberId, targetMemberId);
    }

    /**
     * 특정 회원이 여러 회원들을 팔로우하는지 배치 조회
     *
     * @param currentMemberId 현재 회원 ID
     * @param targetMemberIds 확인할 대상 회원 ID 목록
     * @return 대상 회원 ID별 팔로우 여부 매핑
     */
    public Map<String, Boolean> checkFollowStatusByMemberId(String currentMemberId, List<String> targetMemberIds) {
        if (targetMemberIds == null || targetMemberIds.isEmpty()) {
            return Map.of();
        }

        // 실제로 팔로우하고 있는 대상들을 배치로 조회
        Set<String> followingIds = followRepository.findFollowingIdsByFollowerId(currentMemberId, targetMemberIds);

        // 모든 대상에 대해 팔로우 상태를 설정 (자기 자신은 항상 true)
        return targetMemberIds.stream()
                .distinct()
                .collect(Collectors.toMap(
                        targetId -> targetId,
                        targetId -> Member.isSameMember(currentMemberId, targetId) || followingIds.contains(targetId)
                ));
    }

    /**
     * 특정 회원이 팔로우하는 회원 ID 목록을 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 팔로우하는 회원 ID 목록
     */
    public List<String> retrieveFollowingIds(String memberId) {
        return followRepository.getFollowingMemberIds(memberId);
    }
}
