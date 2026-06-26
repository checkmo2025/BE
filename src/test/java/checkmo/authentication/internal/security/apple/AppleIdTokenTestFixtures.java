package checkmo.authentication.internal.security.apple;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import checkmo.authentication.internal.config.properties.AppleOAuthProperties;
import io.jsonwebtoken.Jwts;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Queue;

final class AppleIdTokenTestFixtures {

    static final String APPLE_ISSUER = "https://appleid.apple.com";
    static final String WEB_CLIENT_ID = "kr.co.checkmo.web";
    static final String IOS_CLIENT_ID = "kr.co.checkmo.app";
    static final String SUBJECT = "001234.abcdef";
    static final String KEY_ID = "apple-key-1";
    static final String SECOND_KEY_ID = "apple-key-2";
    static final Instant NOW = Instant.parse("2026-06-24T00:00:00Z");

    private AppleIdTokenTestFixtures() {
    }

    static AppleIdTokenVerifier verifier(AppleJwks jwks) {
        return verifier(new FakeAppleJwksClient(jwks), Clock.fixed(NOW, ZoneId.of("UTC")));
    }

    static AppleIdTokenVerifier verifier(AppleJwksClient client) {
        return verifier(client, Clock.fixed(NOW, ZoneId.of("UTC")));
    }

    static AppleIdTokenVerifier verifier(AppleJwksClient client, Clock clock) {
        return new AppleIdTokenVerifier(client, appleProperties(), clock);
    }

    static io.jsonwebtoken.JwtBuilder token(String kid, KeyPair keyPair, String audience) {
        return Jwts.builder()
                .header()
                .keyId(kid)
                .and()
                .issuer(APPLE_ISSUER)
                .subject(SUBJECT)
                .audience()
                .add(audience)
                .and()
                .issuedAt(Date.from(NOW.minusSeconds(60)))
                .expiration(Date.from(NOW.plusSeconds(300)))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256);
    }

    static AppleJwks jwks(KeyPair keyPair) {
        return jwks(KEY_ID, keyPair);
    }

    static AppleJwks jwks(String kid, KeyPair keyPair) {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        return new AppleJwks(List.of(new AppleJwk(
                kid,
                "RS256",
                base64UrlUnsigned(publicKey.getModulus()),
                base64UrlUnsigned(publicKey.getPublicExponent())
        )));
    }

    static KeyPair rsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    static void assertInvalidToken(ThrowingCallable callable) {
        assertThatThrownBy(callable::call)
                .isInstanceOf(InvalidAppleIdentityTokenException.class)
                .hasMessage("Invalid Apple identity token");
    }

    private static AppleOAuthProperties appleProperties() {
        AppleOAuthProperties properties = new AppleOAuthProperties();
        properties.setWebClientId(WEB_CLIENT_ID);
        properties.setIosClientId(IOS_CLIENT_ID);
        return properties;
    }

    private static String base64UrlUnsigned(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] unsigned = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, unsigned, 0, unsigned.length);
            bytes = unsigned;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @FunctionalInterface
    interface ThrowingCallable {
        void call() throws Exception;
    }

    static class FakeAppleJwksClient implements AppleJwksClient {

        private final Queue<AppleJwks> responses = new ArrayDeque<>();
        private AppleJwks lastResponse;
        private int fetchCount;

        FakeAppleJwksClient(AppleJwks... responses) {
            this.responses.addAll(List.of(responses));
        }

        @Override
        public AppleJwks fetch() {
            fetchCount++;
            AppleJwks response = responses.poll();
            if (response != null) {
                lastResponse = response;
                return response;
            }
            return lastResponse;
        }

        int fetchCount() {
            return fetchCount;
        }
    }

    static class FailingAppleJwksClient implements AppleJwksClient {

        @Override
        public AppleJwks fetch() {
            throw new RuntimeException("JWKS unavailable");
        }
    }

    static class MutableClock extends Clock {

        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        void setInstant(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
