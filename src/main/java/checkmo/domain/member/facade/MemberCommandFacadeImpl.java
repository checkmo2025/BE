package checkmo.domain.member.facade;

import checkmo.domain.member.service.authenticate.MemberAuthenticationService;
import checkmo.domain.member.service.command.MemberFollowCommandService;
import checkmo.domain.member.service.command.MemberProfileCommandService;
import checkmo.domain.member.service.command.MemberRegistrationCommandService;
import checkmo.domain.member.service.s3.S3Service;
import checkmo.domain.member.web.dto.MemberRequestDTO;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
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
    private final MemberFollowCommandService memberFollowCommandService;
    private final MemberProfileCommandService memberProfileCommandService;
    private final S3Service s3Service;

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
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        memberAuthenticationService.logout(request, response);
    }

    @Override
    public void reactivateMember() {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    @Override
    public MemberResponseDTO.MemberProfileWithCategoryResponseDTO updateMemberProfile(String memberId, MemberRequestDTO.MemberProfileUpdateRequestDTO request) {
        return memberProfileCommandService.updateMemberProfile(memberId, request);
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
    public void followingMember(String memberId, String followingNickname) {
        memberFollowCommandService.followingMember(memberId, followingNickname);
    }

    @Override
    public void unfollowingMember(String memberId, String followingNickname) {
        memberFollowCommandService.unfollowingMember(memberId, followingNickname);
    }

    @Override
    public void deleteFollower(String memberId, String followerNickname) {
        memberFollowCommandService.deleteFollower(memberId, followerNickname);
    }

    @Override
    public MemberResponseDTO.PresignedUrlDTO generateProfileImageUploadUrl(MemberRequestDTO.ImageUploadRequest request) {
        return s3Service.generatePresignedUploadUrl(request.getFileName(), request.getContentType());
    }
}
