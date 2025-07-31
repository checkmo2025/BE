package checkmo.domain.member.service.security.jwt;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 토큰 기반 인증 필터
 *
 * 모든 HTTP 요청에 대해 쿠키에서 JWT 토큰을 추출하고 토큰 검증
 * 액세스 토큰 만료시 리프레시 토큰으로 자동 갱신
 * 토큰 검증 성공시 SecurityContext에 인증 정보 설정
 * JwtTokenProvider 사용해서 토큰 검증 로직 처리
 * -> JwtAuthenticationFilter에서는 토큰을 직접 검증하지 않음. JwtTokenProvider에서 토큰 검증 로직 구현
 */

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
        throws ServletException, IOException {

        String token = resolveToken(request);
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 토큰이 유효한 경우 인증 정보 설정
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null; // 쿠키에서 accessToken을 찾지 못한 경우
    }
}
