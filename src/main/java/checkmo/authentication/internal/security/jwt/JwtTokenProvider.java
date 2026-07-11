package checkmo.authentication.internal.security.jwt;

import java.util.Optional;
import org.springframework.security.core.Authentication;

/**
 * JWT 토큰 생성, 검증 서비스
 *
 * 액세스 토큰과 리프레시 토큰의 생성, 검증, 정보 추출을 담당
 * 토큰 만료 확인, 토큰에서 사용자 정보 추출 등 순수 토큰 로직만 처리
 * 쿠키 설정이나 SecurityContext 처리는 JwtAuthenticationFilter에서 담당
 */
public interface JwtTokenProvider {

    JwtToken generateToken(Authentication authentication);

    JwtToken generateToken(Authentication authentication, String sessionId);

    Authentication getAuthentication(String accessToken);

    boolean validateToken(String token);

    Long getUserIdFromToken(String token);

    String getSessionIdFromToken(String token);

    Optional<String> getExplicitSessionIdFromToken(String token);

    boolean isRefreshTokenValid(String refreshToken);

    Authentication getAuthenticationFromMemberId(Long memberId);

    long getAccessTokenExpirationTime();

    long getRefreshTokenExpirationTime();
}
