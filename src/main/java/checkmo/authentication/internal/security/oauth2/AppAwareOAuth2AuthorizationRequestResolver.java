package checkmo.authentication.internal.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * 네이티브 앱에서 시작한 소셜 로그인을 식별하는 Authorization Request Resolver.
 * <p>
 * 앱은 {@code /oauth2/authorization/{provider}?client=app} 으로 진입한다. 이때 세션에 클라이언트
 * 타입을 저장해 두면, OAuth2 round-trip 후 성공/실패 핸들러가 이를 읽어 웹(쿠키) 대신
 * {@code checkmo://} 딥링크로 응답할 수 있다. (BE 이슈 #263)
 * <p>
 * SecurityConfig가 STATELESS여도 OAuth2 authorization request 저장을 위해 세션은 생성되므로
 * 동일 세션에 플래그를 함께 보관한다.
 */
public class AppAwareOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    public static final String SESSION_CLIENT_TYPE = "OAUTH2_CLIENT_TYPE";
    public static final String CLIENT_TYPE_APP = "app";
    private static final String CLIENT_PARAM = "client";

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    public AppAwareOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = delegate.resolve(request);
        captureClientType(request, authorizationRequest);
        return authorizationRequest;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = delegate.resolve(request, clientRegistrationId);
        captureClientType(request, authorizationRequest);
        return authorizationRequest;
    }

    private void captureClientType(HttpServletRequest request, OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null) {
            return;
        }
        if (CLIENT_TYPE_APP.equalsIgnoreCase(request.getParameter(CLIENT_PARAM))) {
            request.getSession().setAttribute(SESSION_CLIENT_TYPE, CLIENT_TYPE_APP);
        }
    }
}
