package checkmo.authentication.internal.security.apple;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import checkmo.authentication.internal.config.properties.AppleOAuthProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AppleClientSecretGeneratorTest {

    private static final String TEAM_ID = "test-team-id";
    private static final String KEY_ID = "test-key-id";
    private static final String WEB_CLIENT_ID = "test-web-client-id";
    private static final String AUDIENCE = "https://appleid.apple.com";
    private static final Instant NOW = Instant.parse("2026-06-24T00:00:00Z");
    private static final Duration TOKEN_VALIDITY = Duration.ofDays(30);
    private static final Duration REFRESH_SKEW = Duration.ofMinutes(5);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void generatesVerifiableAppleClientSecretWithExpectedHeaderAndClaims() throws Exception {
        KeyPair keyPair = generateKeyPair();
        AppleClientSecretGenerator generator = new AppleClientSecretGenerator(
                appleProperties(keyPair),
                Clock.fixed(NOW, ZoneId.of("UTC"))
        );

        String clientSecret = generator.generateClientSecret();

        Map<String, Object> header = decodeHeader(clientSecret);
        Claims claims = parseClaims(clientSecret, keyPair.getPublic());
        assertSoftly(softly -> {
            softly.assertThat(header.get("alg")).isEqualTo("ES256");
            softly.assertThat(header.get("kid")).isEqualTo(KEY_ID);
            softly.assertThat(claims.getIssuer()).isEqualTo(TEAM_ID);
            softly.assertThat(claims.getSubject()).isEqualTo(WEB_CLIENT_ID);
            softly.assertThat(claims.getAudience()).containsExactly(AUDIENCE);
            softly.assertThat(claims.getIssuedAt()).isEqualTo(Date.from(NOW));
            softly.assertThat(claims.getExpiration()).isEqualTo(Date.from(NOW.plus(TOKEN_VALIDITY)));
        });
    }

    @Test
    void reusesCachedClientSecretBeforeRefreshBoundary() throws Exception {
        KeyPair keyPair = generateKeyPair();
        MutableClock clock = new MutableClock(NOW);
        AppleClientSecretGenerator generator = new AppleClientSecretGenerator(appleProperties(keyPair), clock);

        String first = generator.generateClientSecret();
        clock.setInstant(NOW.plus(TOKEN_VALIDITY).minus(REFRESH_SKEW).minusSeconds(1));
        String second = generator.generateClientSecret();

        assertSoftly(softly -> softly.assertThat(first.equals(second)).isTrue());
    }

    @Test
    void regeneratesClientSecretAtRefreshBoundary() throws Exception {
        KeyPair keyPair = generateKeyPair();
        MutableClock clock = new MutableClock(NOW);
        AppleClientSecretGenerator generator = new AppleClientSecretGenerator(appleProperties(keyPair), clock);

        String first = generator.generateClientSecret();
        Instant refreshBoundary = NOW.plus(TOKEN_VALIDITY).minus(REFRESH_SKEW);
        clock.setInstant(refreshBoundary);
        String refreshed = generator.generateClientSecret();

        Claims refreshedClaims = parseClaims(refreshed, keyPair.getPublic());
        assertSoftly(softly -> {
            softly.assertThat(first.equals(refreshed)).isFalse();
            softly.assertThat(refreshedClaims.getIssuedAt()).isEqualTo(Date.from(refreshBoundary));
            softly.assertThat(refreshedClaims.getExpiration())
                    .isEqualTo(Date.from(refreshBoundary.plus(TOKEN_VALIDITY)));
        });
    }

    @Test
    void rejectsBlankPrivateKeyBase64() {
        AppleOAuthProperties properties = appleProperties(" ");

        AppleClientSecretGenerator generator = new AppleClientSecretGenerator(
                properties,
                Clock.fixed(NOW, ZoneId.of("UTC"))
        );

        assertThatThrownBy(generator::generateClientSecret)
                .isInstanceOf(AppleClientSecretException.class)
                .hasMessage("Apple private key base64 is required");
    }

    @Test
    void rejectsInvalidPrivateKeyBase64() {
        AppleOAuthProperties properties = appleProperties("not-base64 ####");

        AppleClientSecretGenerator generator = new AppleClientSecretGenerator(
                properties,
                Clock.fixed(NOW, ZoneId.of("UTC"))
        );

        assertThatThrownBy(generator::generateClientSecret)
                .isInstanceOf(AppleClientSecretException.class)
                .hasMessage("Apple private key base64 is invalid");
    }

    @Test
    void acceptsLineWrappedPrivateKeyBase64() throws Exception {
        KeyPair keyPair = generateKeyPair();
        AppleClientSecretGenerator generator = new AppleClientSecretGenerator(
                appleProperties(encodeWrappedBase64(privateKeyPem(keyPair))),
                Clock.fixed(NOW, ZoneId.of("UTC"))
        );

        String clientSecret = generator.generateClientSecret();

        Claims claims = parseClaims(clientSecret, keyPair.getPublic());
        assertSoftly(softly -> softly.assertThat(claims.getSubject()).isEqualTo(WEB_CLIENT_ID));
    }

    @Test
    void rejectsPrivateKeyBase64WithIgnoredInvalidCharacters() {
        String encodedPem = Base64.getEncoder().encodeToString("not a pem".getBytes(StandardCharsets.UTF_8));
        AppleOAuthProperties properties = appleProperties(encodedPem + "!!!!");

        AppleClientSecretGenerator generator = new AppleClientSecretGenerator(
                properties,
                Clock.fixed(NOW, ZoneId.of("UTC"))
        );

        assertThatThrownBy(generator::generateClientSecret)
                .isInstanceOf(AppleClientSecretException.class)
                .hasMessage("Apple private key base64 is invalid");
    }

    @Test
    void rejectsMalformedPrivateKeyPem() {
        String encodedPem = Base64.getEncoder().encodeToString("not a pem".getBytes(StandardCharsets.UTF_8));
        AppleOAuthProperties properties = appleProperties(encodedPem);

        AppleClientSecretGenerator generator = new AppleClientSecretGenerator(
                properties,
                Clock.fixed(NOW, ZoneId.of("UTC"))
        );

        assertThatThrownBy(generator::generateClientSecret)
                .isInstanceOf(AppleClientSecretException.class)
                .hasMessage("Apple private key PEM is malformed");
    }

    @Test
    void rejectsPrivateKeyPemWithExtraNonWhitespaceContent() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String pem = "unexpected prefix\n" + privateKeyPem(keyPair) + "\nunexpected suffix";
        AppleOAuthProperties properties = appleProperties(encodeBase64(pem));

        AppleClientSecretGenerator generator = new AppleClientSecretGenerator(
                properties,
                Clock.fixed(NOW, ZoneId.of("UTC"))
        );

        assertThatThrownBy(generator::generateClientSecret)
                .isInstanceOf(AppleClientSecretException.class)
                .hasMessage("Apple private key PEM is malformed");
    }

    @Test
    void rejectsNonParseablePkcs8PrivateKey() {
        String pem = beginMarker() + "\n"
                + "bm90LWtleS1tYXRlcmlhbA==\n"
                + endMarker() + "\n";
        AppleOAuthProperties properties = appleProperties(encodeBase64(pem));

        AppleClientSecretGenerator generator = new AppleClientSecretGenerator(
                properties,
                Clock.fixed(NOW, ZoneId.of("UTC"))
        );

        assertThatThrownBy(generator::generateClientSecret)
                .isInstanceOf(AppleClientSecretException.class)
                .hasMessage("Apple private key could not be parsed");
    }

    @Test
    void instantiatesWithoutCheckmoJwtProperties() throws Exception {
        KeyPair keyPair = generateKeyPair();
        AppleClientSecretGenerator generator = new AppleClientSecretGenerator(
                appleProperties(keyPair),
                Clock.fixed(NOW, ZoneId.of("UTC"))
        );

        String clientSecret = generator.generateClientSecret();

        Claims claims = parseClaims(clientSecret, keyPair.getPublic());
        assertSoftly(softly -> softly.assertThat(claims.getSubject()).isEqualTo(WEB_CLIENT_ID));
    }

    private AppleOAuthProperties appleProperties(KeyPair keyPair) {
        return appleProperties(encodePemBase64(keyPair));
    }

    private AppleOAuthProperties appleProperties(String privateKeyBase64) {
        AppleOAuthProperties properties = new AppleOAuthProperties();
        properties.setTeamId(TEAM_ID);
        properties.setKeyId(KEY_ID);
        properties.setWebClientId(WEB_CLIENT_ID);
        properties.setPrivateKeyBase64(privateKeyBase64);
        return properties;
    }

    private KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        return generator.generateKeyPair();
    }

    private String encodePemBase64(KeyPair keyPair) {
        return encodeBase64(privateKeyPem(keyPair));
    }

    private String privateKeyPem(KeyPair keyPair) {
        String keyBody = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
                .encodeToString(keyPair.getPrivate().getEncoded());
        return beginMarker() + "\n" + keyBody + "\n" + endMarker() + "\n";
    }

    private String encodeBase64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String encodeWrappedBase64(String value) {
        return Base64.getMimeEncoder(32, "\n".getBytes(StandardCharsets.UTF_8))
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String beginMarker() {
        return "-----BEGIN " + "PRIVATE KEY-----";
    }

    private String endMarker() {
        return "-----END " + "PRIVATE KEY-----";
    }

    private Map<String, Object> decodeHeader(String clientSecret) throws Exception {
        String header = new String(Base64.getUrlDecoder().decode(clientSecret.split("\\.")[0]), StandardCharsets.UTF_8);
        return OBJECT_MAPPER.readValue(header, new TypeReference<>() {
        });
    }

    private Claims parseClaims(String clientSecret, PublicKey publicKey) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(clientSecret)
                .getPayload();
    }

    private static class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void setInstant(Instant instant) {
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
