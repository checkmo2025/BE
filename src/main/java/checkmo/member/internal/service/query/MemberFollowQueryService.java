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
    public List<Follow> retrieveFollowers(Long memberId, Long cursorId, int pageSize, List<Long> excludedMemberIds) {
        return followRepository.findFollowers(memberId, cursorId, pageSize, excludedMemberIds);
    }

    /**
     * 특정 회원의 팔로잉 목록 조회
     *
     * @param memberId 조회할 회원의 ID
     * @param cursorId 커서 ID
     * @param pageSize 페이지 크기
     * @return 팔로잉 목록
     */
    public List<Follow> retrieveFollowingIds(Long memberId, Long cursorId, int pageSize, List<Long> excludedMemberIds) {
        return followRepository.findFollowings(memberId, cursorId, pageSize, excludedMemberIds);
    }

    /**
     * 특정 회원의 팔로우 여부 확인
     */
    public boolean isFollowing(Long memberId, Long targetMemberId) {
        if (memberId == null || targetMemberId == null) {
            return false;
        }

        if (Member.isSameMember(memberId, targetMemberId)) {
            return true; // 자기 자신을 팔로우하는 것은 항상 true
        }

        // 팔로우 관계가 존재하는지 확인
        return followRepository.existsByFollow(memberId, targetMemberId);
    }

    /**
     * 특정 회원이 여러 회원들을 팔로우하는지 배치 조회
     *
     * @param currentMemberId 현재 회원 ID
     * @param targetMemberIds 확인할 대상 회원 ID 목록
     * @return 대상 회원 ID별 팔로우 여부 매핑
     */
    public Map<Long, Boolean> checkFollowStatusByMemberId(Long currentMemberId, List<Long> targetMemberIds) {
        if (targetMemberIds == null || targetMemberIds.isEmpty()) {
            return Map.of();
        }

        if (currentMemberId == null) {
            return targetMemberIds.stream()
                    .distinct()
                    .collect(Collectors.toMap(
                            targetId -> targetId,
                            targetId -> false
                    ));
        }

        // 실제로 팔로우하고 있는 대상들을 배치로 조회
        Set<Long> followingIds = followRepository.findFollowingIdsByFollowerId(currentMemberId, targetMemberIds);

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
    public List<Long> retrieveFollowingIds(Long memberId) {
        return followRepository.getFollowingMemberIds(memberId);
    }

    /**
     * 특정 회원의 팔로워 수를 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 팔로워 수
     */
    public long countFollowers(Long memberId) {
        return followRepository.countByFollowing_Id(memberId);
    }

    /**
     * 특정 회원의 팔로잉 수를 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 팔로잉 수
     */
    public long countFollowings(Long memberId) {
        return followRepository.countByFollower_Id(memberId);
    }
}
