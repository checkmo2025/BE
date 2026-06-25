package checkmo.authentication.internal.security.apple;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

final class ApplePrivateKeyLoader {

    private static final String BEGIN_MARKER = "-----BEGIN " + "PRIVATE KEY-----";
    private static final String END_MARKER = "-----END " + "PRIVATE KEY-----";
    private static final Pattern PRIVATE_KEY_PATTERN = Pattern.compile(
            "\\A\\s*" + Pattern.quote(BEGIN_MARKER)
                    + "\\s*([A-Za-z0-9+/=\\s]+)\\s*"
                    + Pattern.quote(END_MARKER) + "\\s*\\z",
            Pattern.DOTALL
    );

    private ApplePrivateKeyLoader() {
    }

    static ECPrivateKey load(String privateKeyBase64) {
        if (!StringUtils.hasText(privateKeyBase64)) {
            throw new AppleClientSecretException("Apple private key base64 is required");
        }

        byte[] pemBytes = decodeOuterBase64(privateKeyBase64);
        String pem = new String(pemBytes, StandardCharsets.UTF_8);
        Matcher matcher = PRIVATE_KEY_PATTERN.matcher(pem);
        if (!matcher.matches()) {
            throw new AppleClientSecretException("Apple private key PEM is malformed");
        }

        byte[] keyBytes = decodePemBody(matcher.group(1));
        return parseEcPrivateKey(keyBytes);
    }

    private static byte[] decodeOuterBase64(String privateKeyBase64) {
        try {
            return Base64.getDecoder().decode(privateKeyBase64.trim());
        } catch (IllegalArgumentException e) {
            throw new AppleClientSecretException("Apple private key base64 is invalid", e);
        }
    }

    private static byte[] decodePemBody(String keyBody) {
        try {
            return Base64.getMimeDecoder().decode(keyBody);
        } catch (IllegalArgumentException e) {
            throw new AppleClientSecretException("Apple private key PEM is malformed", e);
        }
    }

    private static ECPrivateKey parseEcPrivateKey(byte[] keyBytes) {
        try {
            PrivateKey privateKey = KeyFactory.getInstance("EC")
                    .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
            if (privateKey instanceof ECPrivateKey ecPrivateKey) {
                return ecPrivateKey;
            }
            throw new AppleClientSecretException("Apple private key could not be parsed");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AppleClientSecretException("Apple private key could not be parsed", e);
        }
    }
}
