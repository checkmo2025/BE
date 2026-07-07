package checkmo.realtime.web.websocket.interceptor;

import checkmo.authentication.AuthenticationAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * JwtAuthenticationFilter가 설정한 SecurityContext를 WebSocket 세션 속성으로 복사.
 * stateless JWT 환경에서 HttpSessionHandshakeInterceptor 대체용.
 */
@Component
@RequiredArgsConstructor
public class SecurityContextHandshakeInterceptor implements HandshakeInterceptor {

    public static final String APP_REFRESH_TOKEN_HEADER = "X-Refresh-Token";

    private final AuthenticationAPI authenticationAPI;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        SecurityContext context = SecurityContextHolder.getContext();
        if (copyAuthenticatedContext(context, attributes)) {
            return true;
        }

        String refreshToken = request.getHeaders().getFirst(APP_REFRESH_TOKEN_HEADER);
        authenticationAPI.authenticateAppRefreshToken(refreshToken)
                .ifPresent(authentication -> storeAuthentication(authentication, attributes));
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private boolean copyAuthenticatedContext(SecurityContext context, Map<String, Object> attributes) {
        Authentication authentication = context.getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        attributes.put(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        return true;
    }

    private void storeAuthentication(Authentication authentication, Map<String, Object> attributes) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        attributes.put(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }
}
