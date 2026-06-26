package checkmo.authentication.internal.security.apple;

import checkmo.authentication.internal.config.properties.AppleOAuthProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AppleIdTokenVerifier {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_PROVIDER_PREFIX = "APPLE_";
    private static final String EXPECTED_ALGORITHM = "RS256";
    private static final String PRIVATE_RELAY_DOMAIN = "privaterelay.appleid.com";
    private static final Duration JWKS_CACHE_TTL = Duration.ofHours(6);
    private static final Duration ALLOWED_CLOCK_SKEW = Duration.ofSeconds(60);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> JSON_OBJECT = new TypeReference<>() {};

    private final AppleJwksClient jwksClient;
    private final AppleOAuthProperties properties;
    private final Clock clock;
    private Map<String, RSAPublicKey> cachedKeys = Map.of();
    private Instant cacheExpiresAt = Instant.EPOCH;

    @Autowired
    public AppleIdTokenVerifier(AppleJwksClient jwksClient, AppleOAuthProperties properties) {
        this(jwksClient, properties, Clock.systemUTC());
    }

    AppleIdTokenVerifier(AppleJwksClient jwksClient, AppleOAuthProperties properties, Clock clock) {
        this.jwksClient = jwksClient;
        this.properties = properties;
        this.clock = clock;
    }

    public AppleIdentity verifyWebToken(String identityToken) {
        return verify(identityToken, properties.getWebClientId(), null);
    }

    public AppleIdentity verifyIosToken(String identityToken, String rawNonce) {
        if (!StringUtils.hasText(rawNonce)) {
            throw new InvalidAppleIdentityTokenException();
        }
        return verify(identityToken, properties.getIosClientId(), rawNonce);
    }

    static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private AppleIdentity verify(String identityToken, String expectedAudience, String rawNonce) {
        try {
            if (!StringUtils.hasText(identityToken) || !StringUtils.hasText(expectedAudience)) {
                throw new InvalidAppleIdentityTokenException();
            }

            AppleIdTokenParts tokenParts = parseTokenParts(identityToken);
            Map<String, Object> header = decodeJson(tokenParts.header());
            String kid = stringClaim(header, "kid");
            String alg = stringClaim(header, "alg");
            if (!StringUtils.hasText(kid) || !EXPECTED_ALGORITHM.equals(alg)) {
                throw new InvalidAppleIdentityTokenException();
            }

            RSAPublicKey publicKey = publicKey(kid);
            verifySignature(tokenParts, publicKey);

            Map<String, Object> claims = decodeJson(tokenParts.payload());
            validateClaims(claims, expectedAudience, rawNonce);
            String subject = stringClaim(claims, "sub");
            String email = optionalStringClaim(claims, "email");
            boolean emailVerified = booleanClaim(claims, "email_verified");
            boolean privateEmail = booleanClaim(claims, "is_private_email") || isPrivateRelayEmail(email);
            return new AppleIdentity(
                    subject,
                    APPLE_PROVIDER_PREFIX + subject,
                    email,
                    emailVerified,
                    privateEmail,
                    expectedAudience
            );
        } catch (InvalidAppleIdentityTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidAppleIdentityTokenException(e);
        }
    }

    private AppleIdTokenParts parseTokenParts(String identityToken) {
        String[] parts = identityToken.split("\\.", -1);
        if (parts.length != 3
                || !StringUtils.hasText(parts[0])
                || !StringUtils.hasText(parts[1])
                || !StringUtils.hasText(parts[2])) {
            throw new InvalidAppleIdentityTokenException();
        }
        return new AppleIdTokenParts(parts[0], parts[1], parts[2]);
    }

    private Map<String, Object> decodeJson(String base64UrlValue) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(base64UrlValue);
            return OBJECT_MAPPER.readValue(bytes, JSON_OBJECT);
        } catch (Exception e) {
            throw new InvalidAppleIdentityTokenException(e);
        }
    }

    private synchronized RSAPublicKey publicKey(String kid) {
        Instant now = clock.instant();
        if (!now.isBefore(cacheExpiresAt)) {
            refreshKeys(now);
        }

        RSAPublicKey publicKey = cachedKeys.get(kid);
        if (publicKey != null) {
            return publicKey;
        }

        refreshKeys(now);
        publicKey = cachedKeys.get(kid);
        if (publicKey == null) {
            throw new InvalidAppleIdentityTokenException();
        }
        return publicKey;
    }

    private void refreshKeys(Instant now) {
        try {
            AppleJwks jwks = jwksClient.fetch();
            cachedKeys = toPublicKeys(jwks);
            cacheExpiresAt = now.plus(JWKS_CACHE_TTL);
        } catch (Exception e) {
            throw new InvalidAppleIdentityTokenException(e);
        }
    }

    private Map<String, RSAPublicKey> toPublicKeys(AppleJwks jwks) {
        if (jwks == null || jwks.keys() == null) {
            throw new InvalidAppleIdentityTokenException();
        }

        Map<String, RSAPublicKey> keys = new HashMap<>();
        for (AppleJwk jwk : jwks.keys()) {
            if (jwk != null && StringUtils.hasText(jwk.kid()) && EXPECTED_ALGORITHM.equals(jwk.alg())) {
                keys.put(jwk.kid(), toPublicKey(jwk));
            }
        }
        return Map.copyOf(keys);
    }

    private RSAPublicKey toPublicKey(AppleJwk jwk) {
        try {
            BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.n()));
            BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.e()));
            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new RSAPublicKeySpec(modulus, exponent));
        } catch (Exception e) {
            throw new InvalidAppleIdentityTokenException(e);
        }
    }

    private void verifySignature(AppleIdTokenParts tokenParts, RSAPublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(tokenParts.signingInput().getBytes(StandardCharsets.US_ASCII));
            byte[] signatureBytes = Base64.getUrlDecoder().decode(tokenParts.signature());
            if (!signature.verify(signatureBytes)) {
                throw new InvalidAppleIdentityTokenException();
            }
        } catch (InvalidAppleIdentityTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidAppleIdentityTokenException(e);
        }
    }

    private void validateClaims(Map<String, Object> claims, String expectedAudience, String rawNonce) {
        Instant now = clock.instant();
        if (!APPLE_ISSUER.equals(stringClaim(claims, "iss"))) {
            throw new InvalidAppleIdentityTokenException();
        }
        if (!audienceMatches(claims.get("aud"), expectedAudience)) {
            throw new InvalidAppleIdentityTokenException();
        }
        if (!StringUtils.hasText(stringClaim(claims, "sub"))) {
            throw new InvalidAppleIdentityTokenException();
        }
        Instant expiresAt = epochSecondsClaim(claims, "exp");
        if (!expiresAt.isAfter(now)) {
            throw new InvalidAppleIdentityTokenException();
        }
        Instant issuedAt = epochSecondsClaim(claims, "iat");
        if (issuedAt.isAfter(now.plus(ALLOWED_CLOCK_SKEW))) {
            throw new InvalidAppleIdentityTokenException();
        }
        if (StringUtils.hasText(rawNonce)
                && !sha256Hex(rawNonce).equals(stringClaim(claims, "nonce"))) {
            throw new InvalidAppleIdentityTokenException();
        }
    }

    private boolean audienceMatches(Object audience, String expectedAudience) {
        if (audience instanceof String value) {
            return expectedAudience.equals(value);
        }
        if (audience instanceof List<?> values) {
            return values.stream().anyMatch(expectedAudience::equals);
        }
        return false;
    }

    private Instant epochSecondsClaim(Map<String, Object> claims, String name) {
        Object value = claims.get(name);
        if (value instanceof Number number) {
            return Instant.ofEpochSecond(number.longValue());
        }
        throw new InvalidAppleIdentityTokenException();
    }

    private String stringClaim(Map<String, Object> claims, String name) {
        String value = optionalStringClaim(claims, name);
        if (!StringUtils.hasText(value)) {
            throw new InvalidAppleIdentityTokenException();
        }
        return value;
    }

    private String optionalStringClaim(Map<String, Object> claims, String name) {
        Object value = claims.get(name);
        if (value instanceof String stringValue) {
            return stringValue;
        }
        return null;
    }

    private boolean booleanClaim(Map<String, Object> claims, String name) {
        Object value = claims.get(name);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof String stringValue) {
            return Boolean.parseBoolean(stringValue);
        }
        return false;
    }

    private boolean isPrivateRelayEmail(String email) {
        return email != null && email.toLowerCase().endsWith(PRIVATE_RELAY_DOMAIN);
    }

}
