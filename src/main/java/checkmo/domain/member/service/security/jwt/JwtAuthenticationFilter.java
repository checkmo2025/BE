package checkmo.domain.member.service.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 토큰 기반 인증 필터
 *
 * 모든 HTTP 요청에 대해 쿠키에서 JWT 토큰을 추출하고 토큰 검증 액세스 토큰 만료시 리프레시 토큰으로 자동 갱신 토큰 검증 성공시 SecurityContext에 인증
 * 정보 설정 JwtTokenProvider 사용해서 토큰 검증 로직 처리 -> JwtAuthenticationFilter에서는 토큰을 직접 검증하지 않음.
 * JwtTokenProvider에서 토큰 검증 로직 구현
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenCacheService tokenCacheService;
    private final JwtCookieUtil jwtCookieUtil;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
        throws ServletException, IOException {

        // 쿠키에서 Access Token 추출
        String accessToken = resolveToken(request, "accessToken");
        log.info("[JWT 필터] 요청 URI: {}, Access Token 존재 여부 확인: {}", request.getRequestURI(), accessToken != null);

        if (StringUtils.hasText(accessToken))  { // Access Token이 존재하는 경우
            try {
                if (jwtTokenProvider.validateToken(accessToken)) { // Access Token 유효성 검사

                    // Access Token이 유효한 경우, 인증 정보 설정
                    Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("[JWT 필터] Access Token 유효성 검사 통과");
                }
            } catch (ExpiredJwtException e) { // Access Token이 존재하지만 만료된 경우
                log.warn("[JWT 필터] Access Token 만료됨: {}", e.getMessage());
                reissueAccessToken(request, response); // Refresh Token을 사용해 Access Token 재발급 시도
            }
        } else { // Access Token이 존재하지 않는 경우
            log.warn("[JWT 필터] Access Token이 존재하지 않음");
            reissueAccessToken(request, response); // Refresh Token을 사용해 Access Token 재발급 시도
        }

        filterChain.doFilter(request, response); // 다음 필터로 요청 전달
    }

    // Access Token이 만료된 경우, Refresh Token을 사용해 재발급
    private void reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {

        // 쿠키에서 Refresh Token 추출
        String refreshToken = resolveToken(request, "refreshToken");
        log.info("[재발급] Refresh Token 존재 여부 확인: {}", refreshToken != null);

        if (!StringUtils.hasText(refreshToken)) {
            log.warn("재발급 실패: Refresh Token 쿠키 없음");
            return;
        }

        if (!jwtTokenProvider.isRefreshTokenValid(refreshToken)) {
            log.warn("재발급 실패: 유효하지 않은 Refresh Token");
            return;
        }

        String memberId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        if(!StringUtils.hasText(memberId)) {
            log.warn("재발급 실패: Refresh Token에서 memberId 추출 실패");
            return;
        }

        // Redis에 저장된 Refresh Token과 비교
        String storedRefreshToken = tokenCacheService.getRefreshToken(memberId);
        log.info("[재발급] Redis에 저장된 Refresh Token과 비교");

        if (!refreshToken.equals(storedRefreshToken)) {
            log.warn("재발급 실패: 저장된 Refresh Token과 일치하지 않음 (memberId={})", memberId);
            return;
        }

        // Refresh Token이 유효한 경우, 해당 memberId로 인증 정보 가져오기
        Authentication authentication = jwtTokenProvider.getAuthenticationFromMemberId(memberId);

        // 그리고 새로운 Access Token 생성
        JwtToken newJwtToken = jwtTokenProvider.generateToken(authentication);

        // 이제 새로운 Access Token을 쿠키에 담기
        String newAccessToken = newJwtToken.getAccessToken();
        jwtCookieUtil.addTokenToCookie(response, "accessToken", newAccessToken, 2 * 60 * 60); // 2시간 유효

        // SecurityContext에 새로운 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("Access Token 재발급 성공: 새로운 Access Token 생성 (memberId={})", memberId);
    }

    private String resolveToken(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null; // 쿠키에서 Access Token을 찾지 못한 경우
    }

}
