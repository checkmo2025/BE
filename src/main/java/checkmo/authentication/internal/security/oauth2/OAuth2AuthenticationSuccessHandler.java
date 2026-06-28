package checkmo.authentication.internal.security.oauth2;

import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.security.auth.PrincipalDetails;
import checkmo.authentication.internal.security.jwt.JwtLoginProcessor;
import checkmo.authentication.internal.security.jwt.TokenCacheService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 소셜 로그인 성공 후의 성공 처리 핸들러
 * <p>
 * CustomOAuth2UserService에서 인증 성공 후 이 Success Handler로 요청이 자동으로 넘어와서 JWT 토큰 생성, 쿠키 설정등등 작업 수행.
 * 앱(네이티브)에서 시작한 경우(세션에 OAUTH2_CLIENT_TYPE=app)에는 웹 리다이렉트 대신
 * checkmo:// 딥링크로 refreshToken을 전달한다. (BE 이슈 #263)
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtLoginProcessor jwtLoginProcessor;
    private final TokenCacheService tokenCacheService;

    @Value("${app.oauth2.redirect.base-uri}")
    private String baseUri;

    @Value("${app.oauth2.redirect.app-uri}")
    private String appUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        // JWT 토큰 생성 및 쿠키 설정 + refreshToken 확보
        String refreshToken = jwtLoginProcessor.processLogin(response, authentication);

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        AuthUser user = principalDetails.getUser();

        HttpSession session = request.getSession(false);
        boolean isApp = session != null && AppAwareOAuth2AuthorizationRequestResolver.CLIENT_TYPE_APP
                .equals(session.getAttribute(AppAwareOAuth2AuthorizationRequestResolver.SESSION_CLIENT_TYPE));

        clearAuthenticationAttributes(request);

        // 앱: 보안상 refreshToken을 딥링크에 직접 싣지 않고, 단기 일회용 코드만 전달한다.
        // 앱은 이 코드를 POST /auth/app/oauth/exchange 로 보내 refreshToken(바디)으로 교환한다. (이메일 /app/login과 동일 구조)
        if (isApp) {
            session.removeAttribute(AppAwareOAuth2AuthorizationRequestResolver.SESSION_CLIENT_TYPE);
            String oneTimeCode = UUID.randomUUID().toString().replace("-", "");
            tokenCacheService.saveOAuthExchangeCode(oneTimeCode, user.isProfileCompleted() + "|" + refreshToken);
            String appTargetUrl = UriComponentsBuilder.fromUriString(appUri)
                    .queryParam("code", oneTimeCode)
                    .build()
                    .encode()
                    .toUriString();
            getRedirectStrategy().sendRedirect(request, response, appTargetUrl);
            return;
        }

        // 웹: 기존 쿠키 + 웹 리다이렉트
        String targetUrl;
        if (user.isProfileCompleted()) {
            targetUrl = UriComponentsBuilder.fromUriString(baseUri)
                    .path("/")
                    .build()
                    .toUriString();
        } else {
            targetUrl = UriComponentsBuilder.fromUriString(baseUri)
                    .path("/signup/terms")
                    .queryParam("isSocial", principalDetails.isNewSocialSignUp())
                    .build()
                    .toUriString();
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
