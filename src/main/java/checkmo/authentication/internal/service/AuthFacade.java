package checkmo.authentication.internal.service;

import checkmo.authentication.internal.converter.AuthConverter;
import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.security.jwt.JwtLoginProcessor;
import checkmo.authentication.internal.service.command.AuthTokenRotationService;
import checkmo.authentication.internal.service.result.AuthTokenRotationResult;
import checkmo.authentication.internal.service.command.AuthSessionCommandService;
import checkmo.authentication.internal.service.command.AuthUserCommandService;
import checkmo.authentication.internal.service.result.AuthSignUpResult;
import checkmo.authentication.web.dto.AuthRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthFacade {

    private final AuthUserCommandService authUserCommandService;
    private final AuthSessionCommandService authSessionCommandService;
    private final AuthTokenRotationService authTokenRotationService;
    private final JwtLoginProcessor jwtLoginProcessor;

    public AuthSignUpResult signUp(AuthRequestDTO.SignUp request, HttpServletResponse response) {
        AuthUser user = authUserCommandService.signUp(request);

        Authentication authentication = authSessionCommandService
                .login(new AuthRequestDTO.Login(request.getEmail(), request.getPassword()));

        // JWT 토큰 생성 및 쿠키 설정
        jwtLoginProcessor.processLogin(response, authentication);

        return AuthConverter.toSignUpResult(user);
    }

    public String login(AuthRequestDTO.Login request, HttpServletResponse response) {
        Authentication authentication = authSessionCommandService.login(request);

        // JWT 토큰 생성 및 쿠키 설정, Refresh Token 반환
        return jwtLoginProcessor.processLogin(response, authentication);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        authSessionCommandService.logout(request, response);
    }

    public void logoutApp(String refreshToken, HttpServletRequest request, HttpServletResponse response) {
        authSessionCommandService.logoutApp(refreshToken, request, response);
    }

    public String refreshAppToken(String refreshToken, HttpServletResponse response) {
        AuthTokenRotationResult rotationResult = authTokenRotationService.rotateRefreshToken(refreshToken);
        authTokenRotationService.writeTokenCookies(response, rotationResult.getJwtToken());
        return rotationResult.getJwtToken().getRefreshToken();
    }
}
