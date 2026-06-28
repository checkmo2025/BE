package checkmo.authentication.internal.service;

import checkmo.authentication.internal.converter.AuthConverter;
import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.security.jwt.JwtLoginProcessor;
import checkmo.authentication.internal.service.command.AuthTokenRotationService;
import checkmo.authentication.internal.service.result.AuthTokenRotationResult;
import checkmo.authentication.internal.service.command.AuthSessionCommandService;
import checkmo.authentication.internal.service.command.AuthUserCommandService;
import checkmo.authentication.internal.service.result.AuthSignUpResult;
import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.security.jwt.TokenCacheService;
import checkmo.authentication.web.dto.AuthRequestDTO;
import checkmo.authentication.web.dto.AuthResponseDTO;
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
    private final TokenCacheService tokenCacheService;

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

    // 앱 소셜 로그인: 딥링크로 받은 일회용 코드를 refreshToken으로 교환 (이메일 /app/login과 동일하게 바디로 반환)
    public AuthResponseDTO.AppOAuthLogin exchangeOAuthCode(String code) {
        String stored = tokenCacheService.consumeOAuthExchangeCode(code);
        if (stored == null || stored.indexOf('|') < 0) {
            throw new AuthException(AuthErrorStatus.INVALID_OAUTH_CODE);
        }
        int separator = stored.indexOf('|');
        boolean isProfileCompleted = "true".equals(stored.substring(0, separator));
        String refreshToken = stored.substring(separator + 1);
        return AuthResponseDTO.AppOAuthLogin.builder()
                .refreshToken(refreshToken)
                .isProfileCompleted(isProfileCompleted)
                .build();
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
