package checkmo.member.internal.service.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * 소셜 로그인 실패 시 실패 처리 핸들러
 *
 * OAuth2 인증 실패 시 적절한 에러 페이지로 리다이렉트하거나 메인 페이지로 리다이렉트
 */

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {

        log.error("소셜 로그인 인증 실패: {}", exception.getMessage());

        // 실패 시 리다이렉트 URL 설정
        getRedirectStrategy().sendRedirect(request, response, "/login?error=true");
    }
}
