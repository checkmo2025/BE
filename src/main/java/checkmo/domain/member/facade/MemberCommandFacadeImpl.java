package checkmo.domain.member.facade;

import checkmo.domain.member.service.authenticate.MemberAuthenticationService;
import checkmo.domain.member.service.command.MemberRegistrationCommandService;
import checkmo.domain.member.web.dto.MemberRequestDTO;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCommandFacadeImpl implements MemberCommandFacade{

    private final MemberRegistrationCommandService memberRegistrationCommandService;
    private final MemberAuthenticationService memberAuthenticationService;

    @Override
    public void sendEmailVerification(String email) {
        memberRegistrationCommandService.sendEmailVerification(email);
    }

    @Override
    public boolean verifyEmailCode(MemberRequestDTO.EmailVerificationRequestDTO request) {
        return memberRegistrationCommandService.verifyEmailCode(request);
    }

    @Override
    public MemberResponseDTO.SignUpResponseDTO signUp(MemberRequestDTO.SignUpRequestDTO request, HttpServletResponse response) {
        return memberRegistrationCommandService.signUp(request, response);
    }

    @Override
    public void addAdditionalInfo(MemberRequestDTO.AdditionalInfoDTO request) {
        memberRegistrationCommandService.addAdditionalInfo(request);
    }

    @Override
    public MemberResponseDTO.LoginResponseDTO login(MemberRequestDTO.LoginRequestDTO request, HttpServletResponse response) {
        return memberAuthenticationService.login(request, response);
    }

    @Override
    public void logout(String token) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    @Override
    public void reactivateMember() {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    @Override
    public MemberResponseDTO.MemberProfileResponseDTO updateMemberProfile(String memberId, MemberRequestDTO.MemberProfileUpdateRequestDTO request) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    @Override
    public void updatePassword(String memberId, MemberRequestDTO.PasswordUpdateRequestDTO request) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    @Override
    public void deactivateMember(String memberId) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    @Override
    public void deleteMember(String memberId) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    @Override
    public void followMember(String memberId, String targetNickname) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    @Override
    public void unfollowMember(String memberId, String targetNickname) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    @Override
    public void unfollowingMember(String memberId, String targetNickname) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }
}
