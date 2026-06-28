package checkmo.authentication.internal.security.apple;

import static checkmo.authentication.internal.security.apple.AppleIdTokenTestFixtures.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.security.KeyPair;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class AppleIdTokenVerifierTest {

    @Test
    void verifiesWebTokenAndReturnsAppleIdentity() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(keyPair));
        String token = token(KEY_ID, keyPair, WEB_CLIENT_ID)
                .claim("email", "user@example.com")
                .claim("email_verified", "true")
                .compact();

        AppleIdentity identity = verifier.verifyWebToken(token);

        assertSoftly(softly -> {
            softly.assertThat(identity.subject()).isEqualTo(SUBJECT);
            softly.assertThat(identity.providerId()).isEqualTo("APPLE_" + SUBJECT);
            softly.assertThat(identity.email()).isEqualTo("user@example.com");
            softly.assertThat(identity.emailVerified()).isTrue();
            softly.assertThat(identity.privateEmail()).isFalse();
            softly.assertThat(identity.audience()).isEqualTo(WEB_CLIENT_ID);
        });
    }

    @Test
    void verifiesIosTokenWithMatchingNonce() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(keyPair));
        String rawNonce = "mobile-nonce";
        String token = token(KEY_ID, keyPair, IOS_CLIENT_ID)
                .claim("nonce", "5c224040fc94d672b643b5253b957ec929c56e983093788cb44b4c100ae3b97a")
                .compact();

        AppleIdentity identity = verifier.verifyIosToken(token, rawNonce);

        assertSoftly(softly -> softly.assertThat(identity.audience()).isEqualTo(IOS_CLIENT_ID));
    }

    @Test
    void rejectsInvalidAudience() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(keyPair));
        String token = token(KEY_ID, keyPair, "unexpected-client").compact();

        assertInvalidToken(() -> verifier.verifyWebToken(token));
    }

    @Test
    void rejectsInvalidIssuer() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(keyPair));
        String token = token(KEY_ID, keyPair, WEB_CLIENT_ID)
                .issuer("https://example.com")
                .compact();

        assertInvalidToken(() -> verifier.verifyWebToken(token));
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(keyPair));
        String token = token(KEY_ID, keyPair, WEB_CLIENT_ID)
                .expiration(Date.from(NOW.minusSeconds(1)))
                .compact();

        assertInvalidToken(() -> verifier.verifyWebToken(token));
    }

    @Test
    void rejectsWrongSignature() throws Exception {
        KeyPair jwksKeyPair = rsaKeyPair();
        KeyPair signingKeyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(jwksKeyPair));
        String token = token(KEY_ID, signingKeyPair, WEB_CLIENT_ID).compact();

        assertInvalidToken(() -> verifier.verifyWebToken(token));
    }

    @Test
    void rejectsJwksKeyWithWrongKeyType() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(KEY_ID, keyPair, "EC", "sig", "RS256"));
        String token = token(KEY_ID, keyPair, WEB_CLIENT_ID).compact();

        assertInvalidToken(() -> verifier.verifyWebToken(token));
    }

    @Test
    void rejectsJwksKeyWithoutSignatureUse() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(KEY_ID, keyPair, "RSA", "enc", "RS256"));
        String token = token(KEY_ID, keyPair, WEB_CLIENT_ID).compact();

        assertInvalidToken(() -> verifier.verifyWebToken(token));
    }

    @Test
    void refreshesOnceForUnknownKidAndSucceedsWhenRefreshedJwksContainsKey() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        FakeAppleJwksClient client = new FakeAppleJwksClient(
                new AppleJwks(List.of()),
                jwks(keyPair)
        );
        AppleIdTokenVerifier verifier = verifier(client);
        String token = token(KEY_ID, keyPair, WEB_CLIENT_ID).compact();

        AppleIdentity identity = verifier.verifyWebToken(token);

        assertSoftly(softly -> {
            softly.assertThat(identity.subject()).isEqualTo(SUBJECT);
            softly.assertThat(client.fetchCount()).isEqualTo(2);
        });
    }

    @Test
    void refreshesOnceForUnknownKidAndFailsClosedWhenKeyStillMissing() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        FakeAppleJwksClient client = new FakeAppleJwksClient(
                new AppleJwks(List.of()),
                new AppleJwks(List.of())
        );
        AppleIdTokenVerifier verifier = verifier(client);
        String token = token(KEY_ID, keyPair, WEB_CLIENT_ID).compact();

        assertInvalidToken(() -> verifier.verifyWebToken(token));
        assertSoftly(softly -> softly.assertThat(client.fetchCount()).isEqualTo(2));
    }

    @Test
    void rejectsMissingSubject() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(keyPair));
        String token = token(KEY_ID, keyPair, WEB_CLIENT_ID)
                .subject("")
                .compact();

        assertInvalidToken(() -> verifier.verifyWebToken(token));
    }

    @Test
    void acceptsMissingEmail() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(keyPair));
        String token = token(KEY_ID, keyPair, WEB_CLIENT_ID).compact();

        AppleIdentity identity = verifier.verifyWebToken(token);

        assertSoftly(softly -> {
            softly.assertThat(identity.email()).isNull();
            softly.assertThat(identity.emailVerified()).isFalse();
        });
    }

    @Test
    void rejectsNonceMismatch() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(keyPair));
        String token = token(KEY_ID, keyPair, IOS_CLIENT_ID)
                .claim("nonce", "59bcb2470d7a22b8a9f227d96aaf80645e61d5055aa187c1374ed78333d10765")
                .compact();

        assertInvalidToken(() -> verifier.verifyIosToken(token, "mobile-nonce"));
    }

    @Test
    void rejectsIosTokenWhenRawNonceIsNull() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(keyPair));
        String token = token(KEY_ID, keyPair, IOS_CLIENT_ID)
                .claim("nonce", "5c224040fc94d672b643b5253b957ec929c56e983093788cb44b4c100ae3b97a")
                .compact();

        assertInvalidToken(() -> verifier.verifyIosToken(token, null));
    }

    @Test
    void rejectsIosTokenWhenRawNonceIsBlank() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(keyPair));
        String token = token(KEY_ID, keyPair, IOS_CLIENT_ID)
                .claim("nonce", "5c224040fc94d672b643b5253b957ec929c56e983093788cb44b4c100ae3b97a")
                .compact();

        assertInvalidToken(() -> verifier.verifyIosToken(token, "   "));
    }

    @Test
    void rejectsIosTokenWhenTokenNonceIsMissing() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(keyPair));
        String token = token(KEY_ID, keyPair, IOS_CLIENT_ID).compact();

        assertInvalidToken(() -> verifier.verifyIosToken(token, "mobile-nonce"));
    }

    @Test
    void rejectsJwksClientError() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(new FailingAppleJwksClient());
        String token = token(KEY_ID, keyPair, WEB_CLIENT_ID).compact();

        assertInvalidToken(() -> verifier.verifyWebToken(token));
    }

    @Test
    void usesCachedKeyUntilCacheExpiresThenRefreshesAfterSixHours() throws Exception {
        KeyPair firstKeyPair = rsaKeyPair();
        KeyPair secondKeyPair = rsaKeyPair();
        MutableClock clock = new MutableClock(NOW);
        FakeAppleJwksClient client = new FakeAppleJwksClient(
                jwks(KEY_ID, firstKeyPair),
                jwks(SECOND_KEY_ID, secondKeyPair)
        );
        AppleIdTokenVerifier verifier = verifier(client, clock);
        String firstToken = token(KEY_ID, firstKeyPair, WEB_CLIENT_ID)
                .expiration(Date.from(NOW.plus(Duration.ofHours(7))))
                .compact();
        String secondToken = token(SECOND_KEY_ID, secondKeyPair, WEB_CLIENT_ID)
                .issuedAt(Date.from(NOW.plus(Duration.ofHours(6))))
                .expiration(Date.from(NOW.plus(Duration.ofHours(6)).plusSeconds(300)))
                .compact();

        verifier.verifyWebToken(firstToken);
        clock.setInstant(NOW.plus(Duration.ofHours(6)).minusSeconds(1));
        verifier.verifyWebToken(firstToken);
        clock.setInstant(NOW.plus(Duration.ofHours(6)));
        AppleIdentity refreshedIdentity = verifier.verifyWebToken(secondToken);

        assertSoftly(softly -> {
            softly.assertThat(refreshedIdentity.subject()).isEqualTo(SUBJECT);
            softly.assertThat(client.fetchCount()).isEqualTo(2);
        });
    }

    @Test
    void mapsPrivateRelayEmailToPrivateEmail() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(keyPair));
        String token = token(KEY_ID, keyPair, WEB_CLIENT_ID)
                .claim("email", "abc@privaterelay.appleid.com")
                .compact();

        AppleIdentity identity = verifier.verifyWebToken(token);

        assertSoftly(softly -> softly.assertThat(identity.privateEmail()).isTrue());
    }

    @Test
    void mapsApplePrivateEmailClaimToPrivateEmail() throws Exception {
        KeyPair keyPair = rsaKeyPair();
        AppleIdTokenVerifier verifier = verifier(jwks(keyPair));
        String token = token(KEY_ID, keyPair, WEB_CLIENT_ID)
                .claim("email", "user@example.com")
                .claim("is_private_email", "true")
                .compact();

        AppleIdentity identity = verifier.verifyWebToken(token);

        assertSoftly(softly -> softly.assertThat(identity.privateEmail()).isTrue());
    }

}
