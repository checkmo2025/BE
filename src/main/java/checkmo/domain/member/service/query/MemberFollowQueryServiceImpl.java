package checkmo.domain.member.service.query;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.member.repository.FollowRepository;
import checkmo.domain.member.repository.MemberRepository;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberFollowQueryServiceImpl implements MemberFollowQueryService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    @Override
    public MemberResponseDTO.FollowerListResponseDTO getFollowers(String memberId, Long cursorId) {
        return null;
    }

    @Override
    public MemberResponseDTO.FollowingListResponseDTO getFollowing(String memberId, Long cursorId) {
        return null;
    }

    @Override
    public MemberResponseDTO.FollowerListResponseDTO getFollowers(Long memberId, int size) {
        return null;
    }

    @Override
    public MemberResponseDTO.FollowingListResponseDTO getFollowing(Long memberId, int size) {
        return null;
    }

    @Override
    public boolean isFollowing(String memberId, String targetMemberNickname) {
        // 대상 회원의 ID를 조회
        String targetMemberId = memberRepository.findIdByNickName(targetMemberNickname)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 팔로우 관계가 존재하는지 확인
        return followRepository.existsByFollowerIdAndFollowingId(memberId, targetMemberId);
    }
}
