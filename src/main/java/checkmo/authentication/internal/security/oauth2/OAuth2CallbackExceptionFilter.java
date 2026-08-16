package checkmo.authentication.internal.security.oauth2;

import checkmo.authentication.internal.security.jwt.JwtCookieUtil;
import checkmo.common.monitoring.SentryCaptureClient;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2CallbackExceptionFilter extends OncePerRequestFilter {

    private static final String OAUTH2_CALLBACK_PATH = "/login/oauth2/**";

    private final OAuth2AuthenticationFailureHandler failureHandler;
    private final SentryCaptureClient sentryCaptureClient;
    private final JwtCookieUtil jwtCookieUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) {
        return !pathMatcher.match(OAUTH2_CALLBACK_PATH, request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            handleUnexpectedException(request, response, exception);
        }
    }

    private void handleUnexpectedException(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception
    ) throws ServletException, IOException {
        sentryCaptureClient.captureException(exception);
        log.error(
                "OAuth2 콜백 처리 중 예상하지 못한 예외: provider={}, type={}, uri={}",
                provider(request),
                exception.getClass().getSimpleName(),
                request.getRequestURI()
        );
        SecurityContextHolder.clearContext();

        if (response.isCommitted()) {
            rethrow(exception);
            return;
        }

        jwtCookieUtil.deleteTokenFromCookie(response, "accessToken");
        jwtCookieUtil.deleteTokenFromCookie(response, "refreshToken");
        OAuth2AuthenticationException authenticationException = new OAuth2AuthenticationException(
                new OAuth2Error(
                        OAuth2ErrorCodes.OAUTH2_PROCESSING_FAILED,
                        "소셜 로그인 처리 중 오류가 발생했습니다",
                        null
                ),
                "소셜 로그인 처리 중 오류가 발생했습니다",
                exception
        );
        failureHandler.onAuthenticationFailure(request, response, authenticationException);
    }

    private String provider(HttpServletRequest request) {
        String uri = request.getRequestURI();
        int separator = uri.lastIndexOf('/');
        if (separator < 0 || separator == uri.length() - 1) {
            return "unknown";
        }
        String candidate = uri.substring(separator + 1);
        return candidate.matches("[A-Za-z0-9_-]+") ? candidate : "unknown";
    }

    private void rethrow(Exception exception) throws ServletException, IOException {
        if (exception instanceof IOException ioException) {
            throw ioException;
        }
        if (exception instanceof ServletException servletException) {
            throw servletException;
        }
        throw new ServletException(exception);
    }
}
