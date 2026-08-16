package checkmo.authentication.internal.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
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

    private static final String KAKAO_AUTHORIZATION_URI = "/oauth2/authorization/kakao";

    @Value("${app.oauth2.redirect.app-uri}")
    private String appUri;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {

        logAuthenticationFailure(request, exception);

        if (AppleOAuth2AuthorizationRequestResolver.consumeAppClientType(request)) {
            KakaoEmailConsentRetryState.clear(request);
            redirectAppFailure(request, response);
            return;
        }

        if (shouldRetryKakaoEmailConsent(request, exception)) {
            getRedirectStrategy().sendRedirect(request, response, KAKAO_AUTHORIZATION_URI);
            return;
        }

        KakaoEmailConsentRetryState.clear(request);

        // 실패 시 리다이렉트 URL 설정
        getRedirectStrategy().sendRedirect(request, response, "/login?error=true");
    }

    private boolean shouldRetryKakaoEmailConsent(
            HttpServletRequest request,
            AuthenticationException exception
    ) {
        if (!(exception instanceof OAuth2AuthenticationException oauth2Exception)) {
            return false;
        }
        if (!OAuth2ErrorCodes.KAKAO_EMAIL_CONSENT_REQUIRED.equals(
                oauth2Exception.getError().getErrorCode()
        )) {
            return false;
        }
        return KakaoEmailConsentRetryState.beginRetry(request);
    }

    private void logAuthenticationFailure(HttpServletRequest request, AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            OAuth2Error error = oauth2Exception.getError();
            log.error(
                    "소셜 로그인 인증 실패: type={}, uri={}, errorCode={}, description={}, message={}",
                    oauth2Exception.getClass().getSimpleName(),
                    request.getRequestURI(),
                    error.getErrorCode(),
                    sanitize(error.getDescription()),
                    sanitize(exception.getMessage()),
                    exception
            );
            return;
        }

        log.error(
                "소셜 로그인 인증 실패: type={}, uri={}, message={}",
                exception.getClass().getSimpleName(),
                request.getRequestURI(),
                sanitize(exception.getMessage()),
                exception
        );
    }

    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll(
                "(?i)(client_secret|code|id_token|access_token|refresh_token|token)=([^\\s&]+)",
                "$1=***"
        );
    }

    private void redirectAppFailure(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String targetUrl = UriComponentsBuilder.fromUriString(appUri)
                .queryParam("error", "login_failed")
                .build()
                .encode()
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
