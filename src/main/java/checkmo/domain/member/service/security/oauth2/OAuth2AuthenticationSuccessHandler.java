package checkmo.domain.member.service.security.oauth2;

import checkmo.domain.member.service.security.auth.PrincipalDetails;
import checkmo.domain.member.service.security.jwt.JwtLoginProcessor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 소셜 로그인 성공 후의 성공 처리 핸들러
 *
 * CustomOAuth2UserService에서 인증 성공 후 이 Success Handler로 요청이 자동으로 넘어와서
 * JWT 토큰 생성, 쿠키 설정등등 작업 수행
 */

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtLoginProcessor jwtLoginProcessor;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // JWT 토큰 생성 및 쿠키 설정
        jwtLoginProcessor.processLogin(response, authentication);

        // 성공 후 리다이렉트 URL 설정 (프로필 완료 여부에 따라 다르게)
        getRedirectStrategy().sendRedirect(request, response, "/api/auth/redirect/oauth2");
    }
}
