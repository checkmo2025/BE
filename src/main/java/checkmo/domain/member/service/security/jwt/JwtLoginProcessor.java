package checkmo.domain.member.service.security.jwt;

import checkmo.domain.member.service.security.auth.PrincipalDetails;
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

    // 로그인 성공 시 JWT 토큰 생성 및 쿠키 설정
    public void processLogin(HttpServletResponse response, Authentication authentication) {

        // JWT 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        int accessTokenMaxAge = (int) (jwtTokenProvider.getAccessTokenExpirationTime() / 1000L); // ms → sec
        int refreshTokenMaxAge = (int) (jwtTokenProvider.getRefreshTokenExpirationTime() / 1000L);

        jwtCookieUtil.addTokenToCookie(response, "accessToken", jwtToken.getAccessToken(), accessTokenMaxAge);
        jwtCookieUtil.addTokenToCookie(response, "refreshToken", jwtToken.getRefreshToken(), refreshTokenMaxAge);

        // RefreshToken Redis에 저장
        String memberId = ((PrincipalDetails) authentication.getPrincipal()).getMember().getId();
        tokenCacheService.saveRefreshToken(memberId, jwtToken.getRefreshToken());
    }
}

