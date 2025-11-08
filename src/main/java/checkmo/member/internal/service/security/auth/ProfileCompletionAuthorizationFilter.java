package checkmo.member.internal.service.security.auth;

import checkmo.common.apiPayload.ApiResponse;
import checkmo.common.apiPayload.code.status.ErrorStatus;
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
            "/api/auth/logout",
            "/api/auth/additional-info",
            "/api/auth/redirect/oauth2",
            "/api/auth/check-nickname",
            "/api/s3/image/upload-url",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) {
        return excludedPaths.stream()
                            .anyMatch(path -> pathMatcher.match(path, request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
        throws ServletException, IOException {

        // 현재 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
            && authentication.getPrincipal() instanceof PrincipalDetails principalDetails) {

            //  프로필이 완료되지 않은 회원은 에러
            if (!principalDetails.getMember().isProfileCompleted()) {
                log.warn("프로필 미완료 회원 접근 차단: {}, 요청 URI: {}",
                    principalDetails.getMember().getId(), request.getRequestURI());
                sendErrorResponse(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        ApiResponse<Object> errorResponse = ApiResponse.onFailure(
            ErrorStatus.MEMBER_PROFILE_NOT_COMPLETED.getCode(),
            ErrorStatus.MEMBER_PROFILE_NOT_COMPLETED.getMessage(),
            null
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
    }
}
