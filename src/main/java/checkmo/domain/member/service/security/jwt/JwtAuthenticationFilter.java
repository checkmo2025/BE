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

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
        throws ServletException, IOException {

        // 쿠키에서 Access Token 추출
        String accessToken = resolveToken(request, "accessToken");
        log.info("[JWT 필터] 요청 URI: {}, Access Token 존재 여부 확인: {}", request.getRequestURI(), accessToken != null);

        try {
            if (StringUtils.hasText(accessToken) && jwtTokenProvider.validateToken(accessToken)) {
                log.info("[JWT 필터] 유효한 Access Token 발견: {}", accessToken);

                // 토큰이 유효한 경우 인증 정보 설정
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("[JWT 필터] 유효하지 않거나 만료된 Access Token");
            }
        } catch (ExpiredJwtException e) { // Access Token 만료 되었으면 재발급
            log.info("만료된 Access Token, Refresh Token으로 재발급 시도: {}", e.getMessage());
            reissueAccessToken(request, response, accessToken, e);
        }

        filterChain.doFilter(request, response);
    }

    private void reissueAccessToken(HttpServletRequest request, HttpServletResponse response,
                                    String expiredAccessToken,
                                    ExpiredJwtException e) {

        // 사용자 식별 정보 추출
        String memberId = e.getClaims().getSubject();
        log.info("[재발급] memberId 추출: {}", memberId);

        // 쿠키에서 Refresh Token 추출
        String refreshToken = resolveToken(request, "refreshToken");
        log.info("[재발급] Refresh Token 존재 여부 확인: {}", refreshToken != null);

        if (!StringUtils.hasText(refreshToken)) {
            log.warn("재발급 실패: Refresh Token 쿠키 없음 (memberId={})", memberId);
            return;
        }

        // Redis에 저장된 Refresh Token과 비교
        String storedRefreshToken = tokenCacheService.getRefreshToken(memberId);
        log.info("[재발급] Redis에 저장된 Refresh Token: {}", storedRefreshToken);

        if (!refreshToken.equals(storedRefreshToken)) {
            log.warn("재발급 실패: 저장된 Refresh Token과 일치하지 않음 (memberId={})", memberId);
            return;
        }

        // Refresh Token 유효성 검사 (만료 되었는지)
        if (!jwtTokenProvider.isRefreshTokenValid(refreshToken)) {
            log.warn("재발급 실패: Refresh Token 만료됨 (memberId={})", memberId);
            return;
        }

        // 만료된 Access Token으로 인증 객체 생성
        Authentication authentication = jwtTokenProvider.getAuthentication(expiredAccessToken);

        // 그리고 새로운 Access Token 생성
        JwtToken newJwtToken = jwtTokenProvider.generateToken(authentication);

        // 이제 새로운 Access Token을 쿠키에 담기
        String newAccessToken = newJwtToken.getAccessToken();
        addTokenToCookie(response, "accessToken", newAccessToken, 2 * 60 * 60); // 2시간 유효

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

    // TODO: 공통 메서드 분리시키기
    private void addTokenToCookie(HttpServletResponse response, String cookieName, String token,
                                  int maxAge) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true); // 클라이언트 스크립트에서 접근 불가
        cookie.setAttribute("SameSite", "Strict"); // CSRF 공격 방지
        cookie.setPath("/"); // 모든 경로에서 접근 가능
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
