package checkmo.authentication.internal.service;

import checkmo.authentication.internal.converter.AuthConverter;
import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.security.jwt.JwtLoginProcessor;
import checkmo.authentication.internal.security.jwt.TokenCacheService;
import checkmo.authentication.internal.security.oauth2.AppleAppLoginService;
import checkmo.authentication.internal.service.command.AuthTokenRotationService;
import checkmo.authentication.internal.service.command.AuthSessionCommandService;
import checkmo.authentication.internal.service.command.AuthUserCommandService;
import checkmo.authentication.internal.service.result.AuthTokenRotationResult;
import checkmo.authentication.internal.service.result.AuthSignUpResult;
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
    private final AppleAppLoginService appleAppLoginService;
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

    public String loginWithApple(AuthRequestDTO.AppleAppLogin request, HttpServletResponse response) {
        return appleAppLoginService.login(request.getIdentityToken(), request.getRawNonce(), response);
    }

    public AuthResponseDTO.AppOAuthLogin exchangeOAuthCode(AuthRequestDTO.OAuthExchange request) {
        OAuthExchangePayload payload = parseOAuthExchangePayload(
                tokenCacheService.consumeOAuthExchangeCode(request.getCode())
        );

        return AuthResponseDTO.AppOAuthLogin.builder()
                .refreshToken(payload.refreshToken())
                .isProfileCompleted(payload.profileCompleted())
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

    private OAuthExchangePayload parseOAuthExchangePayload(String stored) {
        if (stored == null) {
            throw new AuthException(AuthErrorStatus.INVALID_OAUTH_CODE);
        }

        int separator = stored.indexOf('|');
        if (separator <= 0 || separator == stored.length() - 1) {
            throw new AuthException(AuthErrorStatus.INVALID_OAUTH_CODE);
        }

        String profileCompleted = stored.substring(0, separator);
        if (!"true".equals(profileCompleted) && !"false".equals(profileCompleted)) {
            throw new AuthException(AuthErrorStatus.INVALID_OAUTH_CODE);
        }

        String refreshToken = stored.substring(separator + 1);
        return new OAuthExchangePayload(Boolean.parseBoolean(profileCompleted), refreshToken);
    }

    private record OAuthExchangePayload(boolean profileCompleted, String refreshToken) {
    }
}
