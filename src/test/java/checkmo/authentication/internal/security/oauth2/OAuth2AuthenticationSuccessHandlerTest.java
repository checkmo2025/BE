package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.entity.Role;
import checkmo.authentication.internal.security.auth.PrincipalDetails;
import checkmo.authentication.internal.security.jwt.JwtCookieUtil;
import checkmo.authentication.internal.security.jwt.JwtLoginProcessor;
import checkmo.authentication.internal.security.jwt.JwtToken;
import checkmo.authentication.internal.security.jwt.JwtTokenProvider;
import checkmo.authentication.internal.security.jwt.TokenCacheService;
import checkmo.authentication.internal.service.command.AuthReactivationCommandService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2AuthenticationSuccessHandlerTest {

    private JwtTokenProvider jwtTokenProvider;
    private TokenCacheService tokenCacheService;
    private AuthReactivationCommandService authReactivationCommandService;
    private JwtLoginProcessor jwtLoginProcessor;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = mock(JwtTokenProvider.class);
        tokenCacheService = mock(TokenCacheService.class);
        authReactivationCommandService = mock(AuthReactivationCommandService.class);
        jwtLoginProcessor = new JwtLoginProcessor(
                jwtTokenProvider,
                new JwtCookieUtil(),
                tokenCacheService,
                authReactivationCommandService
        );
    }

    @Test
    void completedProfileRedirectsHomeAndSetsJwtCookies() throws Exception {
        OAuth2AuthenticationSuccessHandler handler = new OAuth2AuthenticationSuccessHandler(
                jwtLoginProcessor,
                tokenCacheService
        );
        ReflectionTestUtils.setField(handler, "baseUri", "https://web.checkmo.test");
        AuthUser user = AuthUser.builder()
                .id("GOOGLE_google-sub")
                .email("social-user@example.com")
                .password("")
                .role(Role.USER)
                .profileCompleted(true)
                .build();
        PrincipalDetails principal = new PrincipalDetails(user, Map.of("sub", "google-sub"), false);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.generateToken(authentication)).thenReturn(JwtToken.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build());
        when(jwtTokenProvider.getAccessTokenExpirationTime()).thenReturn(3_600_000L);
        when(jwtTokenProvider.getRefreshTokenExpirationTime()).thenReturn(86_400_000L);

        handler.onAuthenticationSuccess(request, response, authentication);

        assertSoftly(softly -> {
            softly.assertThat(response.getRedirectedUrl()).isEqualTo("https://web.checkmo.test/");
            softly.assertThat(response.getHeaders("Set-Cookie"))
                    .anyMatch(header -> header.startsWith("accessToken=access-token")
                            && header.contains("HttpOnly")
                            && header.contains("Secure")
                            && header.contains("SameSite=None"))
                    .anyMatch(header -> header.startsWith("refreshToken=refresh-token")
                            && header.contains("HttpOnly")
                            && header.contains("Secure")
                            && header.contains("SameSite=None"));
        });
        verify(authReactivationCommandService).reactivateIfDeactivated("GOOGLE_google-sub");
        verify(tokenCacheService).saveRefreshToken("GOOGLE_google-sub", "refresh-token");
    }

    @Test
    void appAuthorizationRedirectsOneTimeCodeWithoutJwtCookies() throws Exception {
        OAuth2AuthenticationSuccessHandler handler = new OAuth2AuthenticationSuccessHandler(
                jwtLoginProcessor,
                tokenCacheService
        );
        ReflectionTestUtils.setField(handler, "baseUri", "https://web.checkmo.test");
        ReflectionTestUtils.setField(handler, "appUri", "checkmo://oauth-callback");
        AuthUser user = AuthUser.builder()
                .id("APPLE_apple-sub")
                .email("apple-user@example.com")
                .password("")
                .role(Role.USER)
                .profileCompleted(false)
                .build();
        PrincipalDetails principal = new PrincipalDetails(user, Map.of("sub", "apple-sub"), true);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("state", "app-state");
        request.getSession().setAttribute(
                AppleOAuth2AuthorizationRequestResolver.sessionClientTypeKey("app-state"),
                AppleOAuth2AuthorizationRequestResolver.CLIENT_TYPE_APP
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.generateToken(authentication)).thenReturn(JwtToken.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build());

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(tokenCacheService).saveOAuthExchangeCode(codeCaptor.capture(), eq("false|refresh-token"));

        assertSoftly(softly -> {
            softly.assertThat(response.getRedirectedUrl())
                    .startsWith("checkmo://oauth-callback?code=")
                    .contains(codeCaptor.getValue())
                    .doesNotContain("refresh-token")
                    .doesNotContain("access-token");
            softly.assertThat(response.getHeaders("Set-Cookie")).isEmpty();
            softly.assertThat(request.getSession().getAttribute(
                    AppleOAuth2AuthorizationRequestResolver.sessionClientTypeKey("app-state"))).isNull();
            softly.assertThat(codeCaptor.getValue()).isNotBlank();
        });
        verify(authReactivationCommandService).reactivateIfDeactivated("APPLE_apple-sub");
        verify(tokenCacheService).saveRefreshToken("APPLE_apple-sub", "refresh-token");
    }
}
