package checkmo.authentication.internal.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AppRefreshTokenAuthenticationServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenCacheService tokenCacheService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AppRefreshTokenAuthenticationService authenticationService;

    @Test
    void blankTokenDoesNotAuthenticate() {
        Optional<Authentication> result = authenticationService.authenticate(" ");

        assertThat(result).isEmpty();
        verifyNoInteractions(jwtTokenProvider, tokenCacheService);
    }

    @Test
    void invalidSignatureDoesNotAuthenticate() {
        when(jwtTokenProvider.isRefreshTokenValid("refresh-token")).thenReturn(false);

        Optional<Authentication> result = authenticationService.authenticate("refresh-token");

        assertThat(result).isEmpty();
        verify(jwtTokenProvider, never()).getUserIdFromToken("refresh-token");
        verifyNoInteractions(tokenCacheService);
    }

    @Test
    void mismatchedStoredTokenDoesNotAuthenticate() {
        when(jwtTokenProvider.isRefreshTokenValid("refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("refresh-token")).thenReturn(7L);
        when(tokenCacheService.getRefreshToken(7L)).thenReturn("other-token");

        Optional<Authentication> result = authenticationService.authenticate("refresh-token");

        assertThat(result).isEmpty();
        verify(jwtTokenProvider, never()).getAuthenticationFromMemberId(7L);
    }

    @Test
    void missingMemberIdDoesNotQueryTokenCache() {
        when(jwtTokenProvider.isRefreshTokenValid("refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("refresh-token")).thenReturn(null);

        Optional<Authentication> result = authenticationService.authenticate("refresh-token");

        assertThat(result).isEmpty();
        verifyNoInteractions(tokenCacheService);
    }

    @Test
    void matchingStoredTokenAuthenticatesWithoutRotation() {
        when(jwtTokenProvider.isRefreshTokenValid("refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("refresh-token")).thenReturn(7L);
        when(tokenCacheService.getRefreshToken(7L)).thenReturn("refresh-token");
        when(jwtTokenProvider.getAuthenticationFromMemberId(7L)).thenReturn(authentication);

        Optional<Authentication> result = authenticationService.authenticate("refresh-token");

        assertThat(result).containsSame(authentication);
        verify(tokenCacheService, never()).compareAndRotateRefreshToken(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
