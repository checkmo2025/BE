package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
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
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2AuthenticationSuccessHandlerTest {

    @Test
    void completedProfileRedirectsHomeAndSetsJwtCookies() throws Exception {
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        TokenCacheService tokenCacheService = mock(TokenCacheService.class);
        AuthReactivationCommandService authReactivationCommandService = mock(AuthReactivationCommandService.class);
        JwtLoginProcessor jwtLoginProcessor = new JwtLoginProcessor(
                jwtTokenProvider,
                new JwtCookieUtil(),
                tokenCacheService,
                authReactivationCommandService
        );
        OAuth2AuthenticationSuccessHandler handler = new OAuth2AuthenticationSuccessHandler(jwtLoginProcessor);
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
                            && header.contains("SameSite=None"))
                    .anyMatch(header -> header.startsWith("refreshToken=refresh-token")
                            && header.contains("HttpOnly")
                            && header.contains("SameSite=None"));
        });
        verify(authReactivationCommandService).reactivateIfDeactivated("GOOGLE_google-sub");
        verify(tokenCacheService).saveRefreshToken("GOOGLE_google-sub", "refresh-token");
    }
}
