package checkmo.authentication.internal.security.auth;

import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.common.apiPayload.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class ProfileCompletionAuthorizationFilter extends OncePerRequestFilter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final List<String> excludedPaths = List.of(
            "/api/v1/auth/logout",
            "/api/v1/terms",
            "/api/v1/members/me/terms",
            "/api/v1/members/additional-info",
            "/api/v1/auth/redirect/oauth2",
            "/api/v1/members/check-nickname",
            "/api/v1/image/PROFILE/upload-url",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) {
        return excludedPaths.stream()
                .anyMatch(path -> pathMatcher.match(path, request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (isProfileIncomplete(authentication)) {
            sendErrorResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isProfileIncomplete(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof PrincipalDetails principal
                && !principal.getUser().isProfileCompleted();
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        ApiResponse<Object> errorResponse = ApiResponse.onFailure(
                AuthErrorStatus.MEMBER_PROFILE_NOT_COMPLETED.getCode(),
                AuthErrorStatus.MEMBER_PROFILE_NOT_COMPLETED.getMessage(),
                null
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
    }
}
