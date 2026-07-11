package checkmo.authentication.internal.service.command;

import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.security.jwt.JwtCookieUtil;
import checkmo.authentication.internal.security.jwt.JwtToken;
import checkmo.authentication.internal.security.jwt.JwtTokenProvider;
import checkmo.authentication.internal.security.jwt.TokenCacheService;
import checkmo.authentication.internal.service.result.AuthTokenRotationResult;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthTokenRotationService {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenCacheService tokenCacheService;
    private final JwtCookieUtil jwtCookieUtil;

    public AuthTokenRotationResult rotateRefreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken) || !jwtTokenProvider.isRefreshTokenValid(refreshToken)) {
            throw new AuthException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
        }

        Long memberId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        if (memberId == null) {
            throw new AuthException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
        }
        String sessionId = jwtTokenProvider.getSessionIdFromToken(refreshToken);

        Authentication authentication = jwtTokenProvider.getAuthenticationFromMemberId(memberId);
        JwtToken newJwtToken = jwtTokenProvider.generateToken(authentication, sessionId);
        boolean rotated = tokenCacheService.compareAndRotateRefreshToken(
                memberId,
                sessionId,
                refreshToken,
                newJwtToken.getRefreshToken(),
                Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpirationTime())
        );
        if (!rotated) {
            throw new AuthException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
        }

        return new AuthTokenRotationResult(memberId, authentication, newJwtToken);
    }

    public void writeTokenCookies(HttpServletResponse response, JwtToken jwtToken) {
        int accessTokenMaxAge = (int) (jwtTokenProvider.getAccessTokenExpirationTime() / 1000L);
        int refreshTokenMaxAge = (int) (jwtTokenProvider.getRefreshTokenExpirationTime() / 1000L);

        jwtCookieUtil.addTokenToCookie(response, "accessToken", jwtToken.getAccessToken(), accessTokenMaxAge);
        jwtCookieUtil.addTokenToCookie(response, "refreshToken", jwtToken.getRefreshToken(), refreshTokenMaxAge);
    }

    public void writeAppTokenCookie(HttpServletResponse response, JwtToken jwtToken) {
        int accessTokenMaxAge = (int) (jwtTokenProvider.getAccessTokenExpirationTime() / 1000L);

        jwtCookieUtil.addTokenToCookie(response, "accessToken", jwtToken.getAccessToken(), accessTokenMaxAge);
        jwtCookieUtil.deleteTokenFromCookie(response, "refreshToken");
    }
}
