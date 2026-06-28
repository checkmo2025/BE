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

/**
 * 소셜 로그인 실패 시 실패 처리 핸들러
 * <p>
 * OAuth2 인증 실패 시 적절한 에러 페이지로 리다이렉트한다. 앱(네이티브)에서 시작한 경우
 * (세션에 OAUTH2_CLIENT_TYPE=app)에는 checkmo:// 딥링크로 에러를 전달한다. (BE 이슈 #263)
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

        log.error("소셜 로그인 인증 실패: {}", exception.getMessage());

        HttpSession session = request.getSession(false);
        boolean isApp = session != null && AppAwareOAuth2AuthorizationRequestResolver.CLIENT_TYPE_APP
                .equals(session.getAttribute(AppAwareOAuth2AuthorizationRequestResolver.SESSION_CLIENT_TYPE));

        // 앱: 딥링크로 에러 전달
        if (isApp) {
            session.removeAttribute(AppAwareOAuth2AuthorizationRequestResolver.SESSION_CLIENT_TYPE);
            getRedirectStrategy().sendRedirect(request, response, appUri + "?error=login_failed");
            return;
        }

        // 웹: 기존 실패 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, "/login?error=true");
    }
}
