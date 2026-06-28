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
 * CustomOAuth2UserService에서 인증 성공 후 이 Success Handler로 요청이 자동으로 넘어와서 JWT 토큰 생성, 쿠키 설정등등 작업 수행
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

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        AuthUser user = principalDetails.getUser();

        if (isAppClient(request)) {
            handleAppSuccess(request, response, authentication);
            return;
        }

        // JWT 토큰 생성 및 쿠키 설정
        jwtLoginProcessor.processLogin(response, authentication);

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

        // 성공 후 리다이렉트 URL 설정
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private boolean isAppClient(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && AppleOAuth2AuthorizationRequestResolver.CLIENT_TYPE_APP.equals(
                session.getAttribute(AppleOAuth2AuthorizationRequestResolver.SESSION_CLIENT_TYPE)
        );
    }

    private void handleAppSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        AuthUser user = principalDetails.getUser();
        String refreshToken = jwtLoginProcessor.processLoginWithoutCookies(authentication);
        String code = UUID.randomUUID().toString().replace("-", "");
        tokenCacheService.saveOAuthExchangeCode(code, user.isProfileCompleted() + "|" + refreshToken);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(AppleOAuth2AuthorizationRequestResolver.SESSION_CLIENT_TYPE);
        }
        clearAuthenticationAttributes(request);

        String targetUrl = UriComponentsBuilder.fromUriString(appUri)
                .queryParam("code", code)
                .build()
                .encode()
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
