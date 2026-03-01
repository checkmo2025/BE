package checkmo.authentication.internal.security.jwt;

import checkmo.authentication.internal.security.auth.PrincipalDetails;
import checkmo.authentication.internal.service.command.AuthReactivationCommandService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLoginProcessor {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtCookieUtil jwtCookieUtil;
    private final TokenCacheService tokenCacheService;
    private final AuthReactivationCommandService authReactivationCommandService;

    // 로그인 성공 시 JWT 토큰 생성 및 쿠키 설정
    public void processLogin(HttpServletResponse response, Authentication authentication) {
        String userId = ((PrincipalDetails) authentication.getPrincipal()).getUser().getId();

        // 인증 성공 시점에만 계정 자동 복구
        authReactivationCommandService.reactivateIfDeactivated(userId);

        // JWT 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        int accessTokenMaxAge = (int) (jwtTokenProvider.getAccessTokenExpirationTime() / 1000L); // ms → sec
        int refreshTokenMaxAge = (int) (jwtTokenProvider.getRefreshTokenExpirationTime() / 1000L);

        jwtCookieUtil.addTokenToCookie(response, "accessToken", jwtToken.getAccessToken(), accessTokenMaxAge);
        jwtCookieUtil.addTokenToCookie(response, "refreshToken", jwtToken.getRefreshToken(), refreshTokenMaxAge);

        // RefreshToken Redis에 저장
        tokenCacheService.saveRefreshToken(userId, jwtToken.getRefreshToken());
    }
}
