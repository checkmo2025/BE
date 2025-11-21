package checkmo.member.internal.service.query;

import checkmo.member.internal.entity.Follow;
import checkmo.member.internal.repository.FollowRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberFollowQueryServiceImpl implements MemberFollowQueryService {

    private final FollowRepository followRepository;

    @Override
    public List<Follow> getFollowerList(String memberId, Long cursorId, int pageSize) {
        // cursorId가 null인 경우, 가장 최근 팔로워부터 조회, 여기서 memberId = 팔로잉 당하는 사람의 ID
        if (cursorId == null) {
            return followRepository.findByFollowingIdOrderByIdDesc(memberId, PageRequest.of(0, pageSize));
        } else {
            // cursorId보다 작은 ID의 팔로워를 조회
            return followRepository.findByFollowingIdAndIdLessThanOrderByIdDesc(memberId, cursorId,
                    PageRequest.of(0, pageSize));
        }
    }

    @Override
    public List<Follow> getFollowingList(String memberId, Long cursorId, int pageSize) {
        // cursorId가 null인 경우, 가장 최근 팔로잉부터 조회, 여기서 memberId = 팔로우 하는 사람의 ID
        if (cursorId == null) {
            return followRepository.findByFollowerIdOrderByIdDesc(memberId, PageRequest.of(0, pageSize));
        } else {
            // cursorId보다 작은 ID의 팔로잉을 조회
            return followRepository.findByFollowerIdAndIdLessThanOrderByIdDesc(memberId, cursorId,
                    PageRequest.of(0, pageSize));
        }
    }

    @Override
    public List<Follow> getFollowers(String memberId, int size) {
        return followRepository.findByFollowingIdOrderByIdDesc(memberId, PageRequest.of(0, size));
    }

    @Override
    public List<Follow> getFollowings(String memberId, int size) {
        return followRepository.findByFollowerIdOrderByIdDesc(memberId, PageRequest.of(0, size));
    }

    @Override
    public boolean isFollowing(String memberId, String targetMemberId) {
        if (memberId.equals(targetMemberId)) {
            return true; // 자기 자신을 팔로우하는 것은 항상 true
        }

        // 팔로우 관계가 존재하는지 확인
        return followRepository.existsByFollowerIdAndFollowingId(memberId, targetMemberId);
    }

    @Override
    public Map<String, Boolean> getFollowStatusMapForMembers(String currentMemberId, List<String> targetMemberIds) {
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
                        targetId -> currentMemberId.equals(targetId) || followingIds.contains(targetId)
                ));
    }

    @Override
    public List<String> getFollowingMemberIds(String memberId) {
        return followRepository.getFollowingMemberIds(memberId);
    }
}
