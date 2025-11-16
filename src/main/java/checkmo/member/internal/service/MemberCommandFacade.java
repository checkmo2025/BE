package checkmo.member.internal.service;

import checkmo.member.internal.converter.MemberConverter;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.service.command.MemberProfileCommandService;
import checkmo.member.web.dto.MemberRequestDTO;
import checkmo.member.web.dto.MemberResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCommandFacade {

    // 자신의 CommandService
    private final MemberProfileCommandService memberProfileCommandService;

    public void reactivateMember() {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    public MemberResponseDTO.MemberProfileWithCategory updateMemberProfile(
            String memberId,
            MemberRequestDTO.MemberProfileUpdate request
    ) {
        Member updatedMember = memberProfileCommandService.updateMemberProfile(memberId, request);

        return MemberConverter.toMemberProfileWithCategory(updatedMember);
    }

    public void updatePassword(String memberId, MemberRequestDTO.PasswordUpdate request) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    public void deactivateMember(String memberId) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    public void deleteMember(String memberId) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }
}
