package checkmo.authentication.internal.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppRefreshTokenAuthenticationService {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenCacheService tokenCacheService;

    public Optional<Authentication> authenticate(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return Optional.empty();
        }

        try {
            if (!jwtTokenProvider.isRefreshTokenValid(refreshToken)) {
                log.warn("[앱 WS 인증] 유효하지 않은 Refresh Token");
                return Optional.empty();
            }

            Long memberId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            if (memberId == null) {
                log.warn("[앱 WS 인증] Refresh Token에서 memberId를 추출할 수 없음");
                return Optional.empty();
            }
            String sessionId = jwtTokenProvider.getSessionIdFromToken(refreshToken);

            String storedRefreshToken = tokenCacheService.getRefreshToken(memberId, sessionId);
            if (!matchesStoredToken(refreshToken, storedRefreshToken)) {
                log.warn(
                        "[앱 WS 인증] Redis Refresh Token 불일치 - memberId={}, sessionId={}",
                        memberId,
                        sessionId
                );
                return Optional.empty();
            }

            return Optional.of(jwtTokenProvider.getAuthenticationFromMemberId(memberId));
        } catch (RuntimeException e) {
            log.warn("[앱 WS 인증] Refresh Token 인증 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private boolean matchesStoredToken(String providedToken, String storedToken) {
        if (!StringUtils.hasText(storedToken)) {
            return false;
        }
        return MessageDigest.isEqual(
                providedToken.getBytes(StandardCharsets.UTF_8),
                storedToken.getBytes(StandardCharsets.UTF_8)
        );
    }
}
