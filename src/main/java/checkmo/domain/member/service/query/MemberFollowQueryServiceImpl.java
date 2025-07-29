package checkmo.domain.member.service.query;

import checkmo.domain.member.repository.FollowRepository;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberFollowQueryServiceImpl implements MemberFollowQueryService {

    private final FollowRepository followRepository;

    @Override
    public MemberResponseDTO.FollowerListResponseDTO getFollowers(String memberId, Long cursorId) {
        throw new UnsupportedOperationException("아직 개발 중~");
    }

    @Override
    public MemberResponseDTO.FollowingListResponseDTO getFollowing(String memberId, Long cursorId) {
        throw new UnsupportedOperationException("아직 개발 중~");
    }

    @Override
    public MemberResponseDTO.FollowerListResponseDTO getFollowers(Long memberId, int size) {
        throw new UnsupportedOperationException("아직 개발 중~");
    }

    @Override
    public MemberResponseDTO.FollowingListResponseDTO getFollowing(Long memberId, int size) {
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
