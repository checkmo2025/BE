package checkmo.domain.member.facade.impl;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.domain.member.repository.MemberRepository;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import checkmo.global.dto.MemberSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
        return null;
    }

    @Override
    public boolean isFollowing(String memberId, String targetMemberNickname) {
        return false;
    }

    @Override
    public MemberSharedDTO.BasicInfoDTO getMemberBasicInfoForShare(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        return MemberSharedDTO.BasicInfoDTO.builder()
                .nickname(member.getNickName())
                .profileImageUrl(member.getImgUrl())
                .build();
    }

    @Override
    public MemberSharedDTO.WithFollowStatusDTO getMemberWithFollowStatusForShare(String targetMemberId, String currentMemberId) {
        return null;
    }

    @Override
    public Member findMemberReferenceById(String memberId) {
        return null;
    }
}
