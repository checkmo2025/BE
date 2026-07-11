package checkmo.authentication.internal.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;

import checkmo.authentication.internal.config.properties.JwtProperties;
import checkmo.authentication.internal.security.auth.CustomUserDetailsService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

class JwtTokenProviderImplTest {

    private static final String RAW_SECRET = "checkmo-session-test-secret-32bytes";

    private JwtTokenProviderImpl jwtTokenProvider;
    private Authentication authentication;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        JwtProperties.TokenValidity tokenValidity = new JwtProperties.TokenValidity();
        tokenValidity.setAccessToken(7_200_000L);
        tokenValidity.setRefreshToken(2_592_000_000L);
        properties.setTokenValidity(tokenValidity);

        byte[] secretBytes = RAW_SECRET.getBytes(StandardCharsets.UTF_8);
        properties.setSecret(Encoders.BASE64.encode(secretBytes));
        secretKey = Keys.hmacShaKeyFor(secretBytes);

        jwtTokenProvider = new JwtTokenProviderImpl(
                properties,
                mock(CustomUserDetailsService.class)
        );
        authentication = new TestingAuthenticationToken("7", null, "ROLE_USER");
    }

    @Test
    void newLoginCreatesIndependentSessionSharedByAccessAndRefreshTokens() {
        JwtToken first = jwtTokenProvider.generateToken(authentication);
        JwtToken second = jwtTokenProvider.generateToken(authentication);

        assertSoftly(softly -> {
            softly.assertThat(first.getSessionId()).isNotBlank();
            softly.assertThat(jwtTokenProvider.getSessionIdFromToken(first.getAccessToken()))
                    .isEqualTo(first.getSessionId());
            softly.assertThat(jwtTokenProvider.getSessionIdFromToken(first.getRefreshToken()))
                    .isEqualTo(first.getSessionId());
            softly.assertThat(jwtTokenProvider.getExplicitSessionIdFromToken(first.getAccessToken()))
                    .contains(first.getSessionId());
            softly.assertThat(jwtTokenProvider.getExplicitSessionIdFromToken(first.getRefreshToken()))
                    .contains(first.getSessionId());
            softly.assertThat(second.getSessionId()).isNotEqualTo(first.getSessionId());
        });
    }

    @Test
    void refreshRotationKeepsSessionIdWhileReplacingTokenPair() {
        JwtToken original = jwtTokenProvider.generateToken(authentication);
        JwtToken rotated = jwtTokenProvider.generateToken(authentication, original.getSessionId());

        assertSoftly(softly -> {
            softly.assertThat(rotated.getSessionId()).isEqualTo(original.getSessionId());
            softly.assertThat(rotated.getAccessToken()).isNotEqualTo(original.getAccessToken());
            softly.assertThat(rotated.getRefreshToken()).isNotEqualTo(original.getRefreshToken());
            softly.assertThat(jwtTokenProvider.getSessionIdFromToken(rotated.getRefreshToken()))
                    .isEqualTo(original.getSessionId());
        });
    }

    @Test
    void legacyRefreshTokenUsesTokenIdAsSessionId() {
        String legacyToken = Jwts.builder()
                .id("legacy-token-id")
                .subject("7")
                .expiration(new Date(System.currentTimeMillis() + 60_000L))
                .signWith(secretKey)
                .compact();

        assertSoftly(softly -> {
            softly.assertThat(jwtTokenProvider.getSessionIdFromToken(legacyToken))
                    .isEqualTo("legacy-token-id");
            softly.assertThat(jwtTokenProvider.getExplicitSessionIdFromToken(legacyToken))
                    .isEmpty();
        });
    }
}
