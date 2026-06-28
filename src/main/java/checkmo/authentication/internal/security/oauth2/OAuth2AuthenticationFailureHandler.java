package checkmo.authentication.internal.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 소셜 로그인 실패 시 실패 처리 핸들러
 * <p>
 * OAuth2 인증 실패 시 적절한 에러 페이지로 리다이렉트하거나 메인 페이지로 리다이렉트
 */
@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.redirect.app-uri}")
    private String appUri;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {

        log.error("소셜 로그인 인증 실패: {}", exception.getClass().getSimpleName());

        if (isAppClient(request)) {
            redirectAppFailure(request, response);
            return;
        }

        // 실패 시 리다이렉트 URL 설정
        getRedirectStrategy().sendRedirect(request, response, "/login?error=true");
    }

    private boolean isAppClient(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && AppleOAuth2AuthorizationRequestResolver.CLIENT_TYPE_APP.equals(
                session.getAttribute(AppleOAuth2AuthorizationRequestResolver.SESSION_CLIENT_TYPE)
        );
    }

    private void redirectAppFailure(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(AppleOAuth2AuthorizationRequestResolver.SESSION_CLIENT_TYPE);
        }

        String targetUrl = UriComponentsBuilder.fromUriString(appUri)
                .queryParam("error", "login_failed")
                .build()
                .encode()
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
