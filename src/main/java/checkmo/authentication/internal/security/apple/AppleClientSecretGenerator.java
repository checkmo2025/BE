package checkmo.authentication.internal.security.apple;

import checkmo.authentication.internal.config.properties.AppleOAuthProperties;
import io.jsonwebtoken.Jwts;
import java.security.interfaces.ECPrivateKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AppleClientSecretGenerator {

    private static final String APPLE_AUDIENCE = "https://appleid.apple.com";
    private static final Duration TOKEN_VALIDITY = Duration.ofDays(30);
    private static final Duration REFRESH_SKEW = Duration.ofMinutes(5);

    private final AppleOAuthProperties properties;
    private final Clock clock;
    private ECPrivateKey privateKey;
    private CachedClientSecret cachedClientSecret;

    @Autowired
    public AppleClientSecretGenerator(AppleOAuthProperties properties) {
        this(properties, Clock.systemUTC());
    }

    AppleClientSecretGenerator(AppleOAuthProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public synchronized String generateClientSecret() {
        Instant now = clock.instant();
        if (cachedClientSecret != null && now.isBefore(cachedClientSecret.refreshAt())) {
            return cachedClientSecret.value();
        }

        Instant expiration = now.plus(TOKEN_VALIDITY);
        String clientSecret = Jwts.builder()
                .header()
                .keyId(required(properties.getKeyId(), "Apple key ID is required"))
                .and()
                .issuer(required(properties.getTeamId(), "Apple team ID is required"))
                .subject(required(properties.getWebClientId(), "Apple web client ID is required"))
                .claim("aud", APPLE_AUDIENCE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(privateKey(), Jwts.SIG.ES256)
                .compact();
        cachedClientSecret = new CachedClientSecret(clientSecret, expiration.minus(REFRESH_SKEW));
        return clientSecret;
    }

    private ECPrivateKey privateKey() {
        if (privateKey == null) {
            privateKey = ApplePrivateKeyLoader.load(properties.getPrivateKeyBase64());
        }
        return privateKey;
    }

    private String required(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new AppleClientSecretException(message);
        }
        return value;
    }

    private record CachedClientSecret(String value, Instant refreshAt) {
    }
}
