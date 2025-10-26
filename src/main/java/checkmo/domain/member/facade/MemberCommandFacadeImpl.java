package checkmo.domain.member.facade;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.member.converter.MemberConverter;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.entity.MemberCategory;
import checkmo.domain.member.service.authenticate.MemberAuthenticationService;
import checkmo.domain.member.service.command.MemberFollowCommandService;
import checkmo.domain.member.service.command.MemberProfileCommandService;
import checkmo.domain.member.service.command.MemberRegistrationCommandService;
import checkmo.domain.member.service.query.MemberCategoryQueryService;
import checkmo.domain.member.service.security.auth.PrincipalDetails;
import checkmo.domain.member.web.dto.MemberRequestDTO;
import checkmo.domain.member.web.dto.MemberRequestDTO.LoginRequestDTO;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import checkmo.global.dto.CategorySharedDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCommandFacadeImpl implements MemberCommandFacade{

    // 자신의 인증 관련 Service
    private final MemberAuthenticationService memberAuthenticationService;

    // 자신의 CommandService
    private final MemberRegistrationCommandService memberRegistrationCommandService;
    private final MemberFollowCommandService memberFollowCommandService;
    private final MemberProfileCommandService memberProfileCommandService;
    private final MemberCategoryQueryService memberCategoryQueryService;

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

        Member member = memberRegistrationCommandService.signUp(request, response);

        memberAuthenticationService.login(new LoginRequestDTO(request.getEmail(), request.getPassword()), response);
        return MemberConverter.fromMember(member);
    }

    @Override
    public void addAdditionalInfo(MemberRequestDTO.AdditionalInfoDTO request) {

        // 현재 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new GeneralException(ErrorStatus.MEMBER_UNAUTHORIZED);
        }

        // 사용자 정보 추출
        Object principal = authentication.getPrincipal();

        // PrincipalDetails 타입으로 캐스팅
        if (!(principal instanceof PrincipalDetails)) {
            throw new GeneralException(ErrorStatus.MEMBER_UNAUTHORIZED);
        }

        String memberId = ((PrincipalDetails) principal).getMember().getId();

        memberRegistrationCommandService.addAdditionalInfo(memberId, request);
    }

    @Override
    public MemberResponseDTO.LoginResponseDTO login(MemberRequestDTO.LoginRequestDTO request, HttpServletResponse response) {

        Member member = memberAuthenticationService.login(request, response);
        return MemberConverter.fromMemberToLoginResponseDTO(member);
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

        Member updatedMember = memberProfileCommandService.updateMemberProfile(memberId, request);

        List<MemberCategory> categoryList = memberCategoryQueryService.findCategoriesByMember(memberId);
        List<CategorySharedDTO.CategoryInfo> categories = MemberConverter.fromMemberCategoriesToCategoryInfoList(categoryList);

        return MemberConverter.toMemberProfileWithCategoryResponseDTO(updatedMember, categories);
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
}
