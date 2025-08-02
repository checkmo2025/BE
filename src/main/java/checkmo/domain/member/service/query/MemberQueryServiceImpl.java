package checkmo.domain.member.service.query;

import checkmo.apiPayload.exception.GeneralException;
import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.domain.member.converter.MemberConverter;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.repository.MemberRepository;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberQueryServiceImpl implements MemberQueryService {

    private final MemberRepository memberRepository;
    private final MemberFollowQueryService memberFollowQueryService;

    @Override
    public boolean isNicknameDuplicated(String nickname) {
        return memberRepository.existsByNickName(nickname);
    }

    /**
     * 회원 기본 정보 조회
     *
     * @param memberId 회원 ID
     * @return 회원 기본 정보 DTO
     */
    @Override
    public MemberResponseDTO.MemberProfileResponseDTO getMemberBasicInfo(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        return MemberConverter.toMemberProfileResponseDTO(member);
    }

    @Override
    public MemberResponseDTO.otherProfileResponseDTO getOtherProfile(String targetMemberNickname, String memberId) {
        return null;
    }

    @Override
    public String getMemberIdByNickname(String nickname) {
        return memberRepository.findIdByNickName(nickname)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public String getMemberNicknameById(String memberId) {
        return memberRepository.findNicknameById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public Map<String, String> getMemberNicknamesByMemberIds(List<String> memberIds) {
        var results = memberRepository.findIdAndNicknameByIdIn(memberIds);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0], // memberId
                        row -> (String) row[1]  // nickname
                ));
    }

    @Override
    public Map<String, MemberResponseDTO.FollowResponse> getMemberNicknamesAndProfileImagesByMemberIds(String memberId, List<String> memberIds) {
        var results = memberRepository.findIdNicknameAndImgUrlByIdIn(memberIds);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0], // targetMemberId
                        row -> {
                            String targetMemberId = (String) row[0];
                            boolean isFollowing = memberFollowQueryService.isFollowing(memberId, targetMemberId);
                            return MemberResponseDTO.FollowResponse.builder()
                                    .nickname((String) row[1])     // nickname
                                    .profileImageUrl((String) row[2]) // imgUrl
                                    .following(isFollowing) // 실제 팔로우 여부 조회
                                    .build();
                        }
                ));
    }
}
