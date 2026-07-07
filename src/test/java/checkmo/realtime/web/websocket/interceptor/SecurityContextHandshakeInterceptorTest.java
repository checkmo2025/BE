package checkmo.realtime.web.websocket.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import checkmo.authentication.AuthenticationAPI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.socket.WebSocketHandler;

@ExtendWith(MockitoExtension.class)
class SecurityContextHandshakeInterceptorTest {

    @Mock
    private AuthenticationAPI authenticationAPI;

    @Mock
    private Authentication authentication;

    @Mock
    private WebSocketHandler webSocketHandler;

    private SecurityContextHandshakeInterceptor interceptor;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        interceptor = new SecurityContextHandshakeInterceptor(authenticationAPI);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void copiesExistingSecurityContextWithoutAppRefreshTokenLookup() {
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        Map<String, Object> attributes = new HashMap<>();

        interceptor.beforeHandshake(request(null), response(), webSocketHandler, attributes);

        assertThat(attributes)
                .containsEntry(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        verifyNoInteractions(authenticationAPI);
    }

    @Test
    void authenticatesAppRefreshTokenHeaderIntoWebSocketSecurityContext() {
        when(authenticationAPI.authenticateAppRefreshToken("refresh-token"))
                .thenReturn(Optional.of(authentication));
        Map<String, Object> attributes = new HashMap<>();

        interceptor.beforeHandshake(request("refresh-token"), response(), webSocketHandler, attributes);

        SecurityContext context = (SecurityContext) attributes.get(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
        );
        assertThat(context.getAuthentication()).isSameAs(authentication);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
    }

    @Test
    void leavesHandshakeUnauthenticatedWithoutAppRefreshTokenHeader() {
        when(authenticationAPI.authenticateAppRefreshToken(null)).thenReturn(Optional.empty());
        Map<String, Object> attributes = new HashMap<>();

        interceptor.beforeHandshake(request(null), response(), webSocketHandler, attributes);

        assertThat(attributes)
                .doesNotContainKey(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private ServerHttpRequest request(String refreshToken) {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/ws-stomp");
        if (refreshToken != null) {
            servletRequest.addHeader(SecurityContextHandshakeInterceptor.APP_REFRESH_TOKEN_HEADER, refreshToken);
        }
        return new ServletServerHttpRequest(servletRequest);
    }

    private ServerHttpResponse response() {
        return new ServletServerHttpResponse(new MockHttpServletResponse());
    }
}
