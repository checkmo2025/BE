package checkmo.authentication.internal.service;

import checkmo.authentication.internal.converter.AuthConverter;
import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.security.auth.PrincipalDetails;
import checkmo.authentication.internal.security.jwt.JwtLoginProcessor;
import checkmo.authentication.internal.service.command.AuthSessionCommandService;
import checkmo.authentication.internal.service.command.SignUpCommandService;
import checkmo.authentication.web.dto.AuthRequestDTO;
import checkmo.authentication.web.dto.AuthResponseDTO;
import checkmo.member.MemberAPI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthFacade {

    private final MemberAPI memberAPI;
    private final SignUpCommandService signUpCommandService;
    private final AuthSessionCommandService authSessionCommandService;
    private final JwtLoginProcessor jwtLoginProcessor;

    public AuthResponseDTO.SignUp signUp(AuthRequestDTO.SignUp request, HttpServletResponse response) {

        AuthUser user = signUpCommandService.signUp(request);

        Authentication authentication = authSessionCommandService
                .login(new AuthRequestDTO.Login(request.getEmail(), request.getPassword()));

        // JWT 토큰 생성 및 쿠키 설정
        jwtLoginProcessor.processLogin(response, authentication);

        return AuthConverter.fromUserToSignUp(user);
    }

    public AuthResponseDTO.Login login(AuthRequestDTO.Login request, HttpServletResponse response) {

        Authentication authentication = authSessionCommandService.login(request);

        // JWT 토큰 생성 및 쿠키 설정
        jwtLoginProcessor.processLogin(response, authentication);

        AuthUser user = ((PrincipalDetails) authentication.getPrincipal()).getUser();

        String userNickname = memberAPI.getMemberNicknameById(user.getId());

        return AuthConverter.fromNicknameToLogin(userNickname);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        authSessionCommandService.logout(request, response);
    }
}
