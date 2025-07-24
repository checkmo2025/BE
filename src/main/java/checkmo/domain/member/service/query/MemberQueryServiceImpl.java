package checkmo.domain.member.service.query;

import checkmo.apiPayload.exception.GeneralException;
import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.domain.member.converter.MemberConverter;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.repository.MemberRepository;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberQueryServiceImpl implements MemberQueryService {

    private final MemberRepository memberRepository;

    @Override
    public boolean isNicknameDuplicated(String nickname) {
        return false;
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
        return null;
    }
}
