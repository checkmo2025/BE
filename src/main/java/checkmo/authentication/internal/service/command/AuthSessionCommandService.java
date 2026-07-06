package checkmo.authentication.internal.service.command;

import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.security.jwt.JwtCookieUtil;
import checkmo.authentication.internal.security.jwt.JwtTokenProvider;
import checkmo.authentication.internal.security.jwt.TokenCacheService;
import checkmo.authentication.web.dto.AuthRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthSessionCommandService {

    // 인증 관련 서비스
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenCacheService tokenCacheService;
    private final JwtCookieUtil jwtCookieUtil;

    public Authentication login(AuthRequestDTO.Login request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword());

        Authentication authentication;

        try {
            // 인증 요청
            authentication = authenticationManager.authenticate(authenticationToken);

            /// 인증 성공 후 SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (AuthenticationException authEx) {
            // 인증 실패 시 예외 처리
            throw new AuthException(AuthErrorStatus.INVALID_CREDENTIALS);
        } catch (Exception e) {
            // 기타 예외 처리
            throw new AuthException(AuthErrorStatus.INTERNAL_SERVER_ERROR);
        }

        // 인증 성공 후 Authentication 객체 반환
        return authentication;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 jwt 토큰 가져오기
        String accessToken = jwtCookieUtil.resolveToken(request, "accessToken");
        String refreshToken = jwtCookieUtil.resolveToken(request, "refreshToken");

        // 2. jwt 토큰을 쿠키에서 삭제
        jwtCookieUtil.deleteTokenFromCookie(response, "accessToken");
        jwtCookieUtil.deleteTokenFromCookie(response, "refreshToken");

        // 3. redis에 저장된 Access Token을 블랙리스트에 추가하여 무효화
        if (StringUtils.hasText(accessToken)) {
            try {
                tokenCacheService.saveBlacklistToken(accessToken);
            } catch (Exception e) {
                log.error("[로그아웃] AccessToken 블랙리스트 저장 실패", e);
            }
        }

        // 4. redis에 저장된 Refresh Token을 redis에서 삭제
        if (StringUtils.hasText(refreshToken)) {
            try {
                if (jwtTokenProvider.isRefreshTokenValid(refreshToken)) {
                    Long memberId = jwtTokenProvider.getUserIdFromToken(refreshToken);
                    tokenCacheService.deleteRefreshTokenIfMatches(memberId, refreshToken);
                }
            } catch (Exception e) {
                log.error("[로그아웃] RefreshToken 삭제 실패", e);
            }
        }
    }

    public void logoutApp(String refreshToken, HttpServletRequest request, HttpServletResponse response) {
        if (!StringUtils.hasText(refreshToken) || !jwtTokenProvider.isRefreshTokenValid(refreshToken)) {
            throw new AuthException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
        }

        try {
            Long memberId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            if (memberId == null) {
                throw new AuthException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
            }
            if (!tokenCacheService.deleteRefreshTokenIfMatches(memberId, refreshToken)) {
                throw new AuthException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
            }
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("[앱 로그아웃] RefreshToken 삭제 실패", e);
            throw new AuthException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
        }

        String accessToken = jwtCookieUtil.resolveToken(request, "accessToken");

        jwtCookieUtil.deleteTokenFromCookie(response, "accessToken");
        jwtCookieUtil.deleteTokenFromCookie(response, "refreshToken");

        if (StringUtils.hasText(accessToken)) {
            try {
                tokenCacheService.saveBlacklistToken(accessToken);
            } catch (Exception e) {
                log.error("[앱 로그아웃] AccessToken 블랙리스트 저장 실패", e);
            }
        }
    }

    public void reactivateMember() {
        // TODO: 계정 복구 로직 구현
    }
}
