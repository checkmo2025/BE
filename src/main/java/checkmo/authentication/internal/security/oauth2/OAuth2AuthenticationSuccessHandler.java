package checkmo.authentication.internal.security.oauth2;

import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.security.auth.PrincipalDetails;
import checkmo.authentication.internal.security.jwt.JwtLoginProcessor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 소셜 로그인 성공 후의 성공 처리 핸들러
 * <p>
 * CustomOAuth2UserService에서 인증 성공 후 이 Success Handler로 요청이 자동으로 넘어와서 JWT 토큰 생성, 쿠키 설정등등 작업 수행
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtLoginProcessor jwtLoginProcessor;

    @Value("${app.oauth2.redirect.base-uri}")
    private String baseUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        // JWT 토큰 생성 및 쿠키 설정
        jwtLoginProcessor.processLogin(response, authentication);

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        AuthUser user = principalDetails.getUser();

        String path = user.isProfileCompleted() ? "/" : "/signup/terms";

        // 기본 리다이렉트 URI
        String targetUrl = UriComponentsBuilder.fromUriString(baseUri)
                .pathSegment(path)
                .build().toUriString();

        // 성공 후 리다이렉트 URL 설정
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
