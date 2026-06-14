package checkmo.authentication.internal.security.jwt;

import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.repository.AuthRepository;
import checkmo.authentication.internal.service.command.AuthTokenRotationService;
import checkmo.authentication.internal.service.result.AuthTokenRotationResult;
import checkmo.common.apiPayload.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 토큰 기반 인증 필터
 * <p>
 * 모든 HTTP 요청에 대해 쿠키에서 JWT 토큰을 추출하고 토큰 검증 액세스 토큰 만료시 리프레시 토큰으로 자동 갱신 토큰 검증 성공시 SecurityContext에 인증 정보 설정
 * JwtTokenProvider 사용해서 토큰 검증 로직 처리 -> JwtAuthenticationFilter에서는 토큰을 직접 검증하지 않음. JwtTokenProvider에서 토큰 검증 로직 구현
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenCacheService tokenCacheService;
    private final JwtCookieUtil jwtCookieUtil;
    private final AuthRepository authRepository;
    private final AuthTokenRotationService authTokenRotationService;
    private final ObjectMapper objectMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final List<String> excludedPaths = List.of(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/auth/**",
            "/api/members/check-nickname",
            "/api/members/find-email",
            "/health"
    );

    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) {
        return excludedPaths.stream()
                .anyMatch(path -> pathMatcher.match(path, request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain
    ) throws ServletException, IOException {
        // 쿠키에서 Access Token 추출
        String accessToken = jwtCookieUtil.resolveToken(request, "accessToken");
        log.info("[JWT 필터] 요청 URI: {}, Access Token 존재 여부 확인: {}", request.getRequestURI(),
                accessToken != null);

        if (!StringUtils.hasText(accessToken)) {
            log.warn("[JWT 필터] Access Token이 존재하지 않음");
            reissueAccessToken(request, response); // Refresh Token을 사용해 재발급 시도
            filterChain.doFilter(request, response);
            return;
        }

        // Access Token이 존재하는 경우
        try {
            if (jwtTokenProvider.validateToken(accessToken)) { // Access Token 유효성 검사

                if (tokenCacheService.isAccessTokenBlacklisted(accessToken)) {
                    log.warn("[JWT 필터] 블랙리스트에 등록된 Access Token 입니다. 요청 거부.");
                    SecurityContextHolder.clearContext();
                    //filterChain.doFilter(request, response); // 인증 없이 계속 진행
                    sendErrorResponse(response, AuthErrorStatus.TOKEN_BLACKLISTED);
                    return;
                }

                // 유령 회원(삭제된 회원) 여부 확인
                String memberId = jwtTokenProvider.getUserIdFromToken(accessToken);
                if (!authRepository.existsById(memberId)) {
                    log.warn("[JWT 필터] 존재하지 않는 계정(유령 회원) 감지: memberId={}", memberId);
                    tokenCacheService.saveBlacklistToken(accessToken);
                    sendErrorResponse(response, AuthErrorStatus.GHOST_MEMBER_CLEANED_UP);
                    return;
                }

                // Access Token이 유효한 경우, 인증 정보 설정
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("[JWT 필터] Access Token 유효성 검사 통과");
            }
        } catch (AuthException e) {
            log.warn("[JWT 필터] 비활성/유효하지 않은 회원 토큰 감지: {}", e.getMessage());
            clearInvalidSession(response, accessToken);
            SecurityContextHolder.clearContext();
        } catch (ExpiredJwtException e) { // Access Token이 존재하지만 만료된 경우
            log.warn("[JWT 필터] Access Token 만료됨: {}", e.getMessage());
            reissueAccessToken(request, response); // Refresh Token을 사용해 재발급 시도
        }

        filterChain.doFilter(request, response);
    }

    private void clearInvalidSession(HttpServletResponse response, String accessToken) {
        if (StringUtils.hasText(accessToken)) {
            tokenCacheService.saveBlacklistToken(accessToken);
        }
        jwtCookieUtil.deleteTokenFromCookie(response, "accessToken");
        jwtCookieUtil.deleteTokenFromCookie(response, "refreshToken");
    }

    // Access Token이 만료된 경우, Refresh Token을 사용해 재발급
    private void reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 Refresh Token 추출 (웹)
        String refreshToken = jwtCookieUtil.resolveToken(request, "refreshToken");
        log.info("[재발급] Refresh Token 존재 여부 확인: {}", refreshToken != null);

        if (!StringUtils.hasText(refreshToken)) {
            log.warn("재발급 실패: Refresh Token 쿠키 없음");
            return;
        }

        try {
            AuthTokenRotationResult rotationResult = authTokenRotationService.rotateRefreshToken(refreshToken);
            authTokenRotationService.writeTokenCookies(response, rotationResult.getJwtToken());
            SecurityContextHolder.getContext().setAuthentication(rotationResult.getAuthentication());
            log.info("Access Token + Refresh Token 재발급 성공 (Rotation, memberId={})", rotationResult.getMemberId());
        } catch (AuthException e) {
            log.warn("재발급 실패: {}", e.getMessage());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, AuthErrorStatus status) throws IOException {
        jwtCookieUtil.deleteTokenFromCookie(response, "accessToken");
        jwtCookieUtil.deleteTokenFromCookie(response, "refreshToken");

        response.setCharacterEncoding("UTF-8");
        response.setStatus(status.getHttpStatus().value());; // 401 Unauthorized
        response.setContentType("application/json");

        ApiResponse<Object> errorResponse = ApiResponse.onFailure(
            status.getCode(),
            status.getMessage(),
            null
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
