package checkmo.member.internal.service;

import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.internal.converter.MemberConverter;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.service.authenticate.MemberAuthenticationService;
import checkmo.member.internal.service.command.MemberProfileCommandService;
import checkmo.member.internal.service.command.MemberRegistrationCommandService;
import checkmo.member.internal.service.security.auth.PrincipalDetails;
import checkmo.member.internal.service.security.jwt.JwtLoginProcessor;
import checkmo.member.web.dto.MemberRequestDTO;
import checkmo.member.web.dto.MemberRequestDTO.LoginRequest;
import checkmo.member.web.dto.MemberResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCommandFacade {

    // 자신의 인증 관련 Service
    private final MemberAuthenticationService memberAuthenticationService;

    // 자신의 CommandService
    private final MemberRegistrationCommandService memberRegistrationCommandService;
    private final MemberProfileCommandService memberProfileCommandService;
    private final JwtLoginProcessor jwtLoginProcessor;

    public MemberResponseDTO.SignUpResponse signUp(
            MemberRequestDTO.SignUpRequest request,
            HttpServletResponse response
    ) {

        Member member = memberRegistrationCommandService.signUp(request);

        Authentication authentication = memberAuthenticationService
                .login(new LoginRequest(request.getEmail(), request.getPassword()));

        // JWT 토큰 생성 및 쿠키 설정
        jwtLoginProcessor.processLogin(response, authentication);

        return MemberConverter.fromMember(member);
    }

    public void addAdditionalInfo(MemberRequestDTO.AdditionalInfo request) {

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

    public MemberResponseDTO.LoginResponse login(
            LoginRequest request,
            HttpServletResponse response
    ) {
        Authentication authentication = memberAuthenticationService.login(request);

        // JWT 토큰 생성 및 쿠키 설정
        jwtLoginProcessor.processLogin(response, authentication);

        Member member = ((PrincipalDetails) authentication.getPrincipal()).getMember();
        return MemberConverter.fromMemberToLoginResponse(member);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        memberAuthenticationService.logout(request, response);
    }

    public void reactivateMember() {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    public MemberResponseDTO.MemberProfileWithCategory updateMemberProfile(
            String memberId,
            MemberRequestDTO.MemberProfileUpdateRequest request
    ) {
        Member updatedMember = memberProfileCommandService.updateMemberProfile(memberId, request);

        return MemberConverter.toMemberProfileWithCategory(updatedMember);
    }

    public void updatePassword(String memberId, MemberRequestDTO.PasswordUpdateRequest request) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    public void deactivateMember(String memberId) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    public void deleteMember(String memberId) {
        throw new UnsupportedOperationException("추후 구현 예정");
    }
}
