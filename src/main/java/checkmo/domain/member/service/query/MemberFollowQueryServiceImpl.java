package checkmo.domain.member.service.query;

import checkmo.domain.member.entity.Follow;
import checkmo.domain.member.repository.FollowRepository;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

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
            return followRepository.findByFollowingIdAndIdLessThanOrderByIdDesc(memberId, cursorId, PageRequest.of(0, pageSize));
        }
    }

    @Override
    public List<Follow> getFollowingList(String memberId, Long cursorId, int pageSize) {

        // cursorId가 null인 경우, 가장 최근 팔로잉부터 조회, 여기서 memberId = 팔로우 하는 사람의 ID
        if (cursorId == null) {
            return followRepository.findByFollowerIdOrderByIdDesc(memberId, PageRequest.of(0, pageSize));
        } else {
            // cursorId보다 작은 ID의 팔로잉을 조회
            return followRepository.findByFollowerIdAndIdLessThanOrderByIdDesc(memberId, cursorId, PageRequest.of(0, pageSize));
        }
    }

    @Override
    public MemberResponseDTO.FollowList getFollowers(Long memberId, int size) {
        throw new UnsupportedOperationException("아직 개발 중~");
    }

    @Override
    public MemberResponseDTO.FollowList getFollowings(Long memberId, int size) {
        throw new UnsupportedOperationException("아직 개발 중~");
    }

    @Override
    public boolean isFollowing(String memberId, String targetMemberId) {
        if (memberId.equals(targetMemberId)) {
            return true; // 자기 자신을 팔로우하는 것은 항상 true
        }

        // 팔로우 관계가 존재하는지 확인
        return followRepository.existsByFollowerIdAndFollowingId(memberId, targetMemberId);
    }
}
