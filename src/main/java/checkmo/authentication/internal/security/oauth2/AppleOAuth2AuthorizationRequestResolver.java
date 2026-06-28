package checkmo.authentication.internal.security.oauth2;

import checkmo.authentication.internal.entity.Provider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

public class AppleOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final String AUTHORIZATION_REQUEST_BASE_URI = "/oauth2/authorization";
    private static final String RESPONSE_MODE = "response_mode";
    private static final String FORM_POST = "form_post";
    private static final String CLIENT_PARAM = "client";
    public static final String SESSION_CLIENT_TYPE = "OAUTH2_CLIENT_TYPE";
    public static final String CLIENT_TYPE_APP = "app";

    private final OAuth2AuthorizationRequestResolver delegate;

    public AppleOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                AUTHORIZATION_REQUEST_BASE_URI
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = delegate.resolve(request);
        captureClientType(request, authorizationRequest);
        return customize(authorizationRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = delegate.resolve(request, clientRegistrationId);
        captureClientType(request, authorizationRequest);
        return customize(authorizationRequest);
    }

    private void captureClientType(HttpServletRequest request, OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null) {
            return;
        }

        if (CLIENT_TYPE_APP.equalsIgnoreCase(request.getParameter(CLIENT_PARAM))) {
            request.getSession().setAttribute(SESSION_CLIENT_TYPE, CLIENT_TYPE_APP);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(SESSION_CLIENT_TYPE);
        }
    }

    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null || !isAppleRegistration(authorizationRequest)) {
            return authorizationRequest;
        }

        Map<String, Object> additionalParameters = new LinkedHashMap<>(
                authorizationRequest.getAdditionalParameters()
        );
        additionalParameters.put(RESPONSE_MODE, FORM_POST);

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
    }

    private boolean isAppleRegistration(OAuth2AuthorizationRequest authorizationRequest) {
        Object registrationId = authorizationRequest.getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
        return Provider.APPLE.equals(registrationId);
    }
}
