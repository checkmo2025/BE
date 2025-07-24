package checkmo.domain.member.facade;

import checkmo.domain.member.entity.Member;
import checkmo.domain.member.repository.MemberRepository;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import checkmo.global.dto.MemberSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryFacadeImpl implements MemberQueryFacade {

    private final MemberRepository memberRepository;

    @Override
    public boolean isNicknameDuplicated(String nickname) {
        return false;
    }

    @Override
    public MemberResponseDTO.MemberProfileResponseDTO getMemberBasicInfo(String memberId) {
        return null;
    }

    @Override
    public MemberResponseDTO.otherProfileResponseDTO getOtherProfile(String targetMemberNickname, String memberId) {
        return null;
    }

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
    public String getMemberIdByNickname(String nickname) {
        return "";
    }

    @Override
    public boolean isFollowing(String memberId, String targetMemberNickname) {
        return false;
    }

    @Override
    public MemberSharedDTO.BasicInfoDTO getMemberBasicInfoForShare(String memberId) {
        return null;
    }

    @Override
    public MemberSharedDTO.WithFollowStatusDTO getMemberWithFollowStatusForShare(String targetMemberId, String currentMemberId) {
        return MemberSharedDTO.WithFollowStatusDTO.builder().build();
    }

    @Override
    public Member findMemberReferenceById(String memberId) {
        return memberRepository.getReferenceById(memberId);
    }
}
