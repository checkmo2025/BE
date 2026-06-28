package checkmo.authentication.internal.security.apple;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.util.StringUtils;

public record AppleIdentity(
        String subject,
        String providerId,
        String email,
        boolean emailVerified,
        boolean privateEmail,
        String audience
) {

    private static final String EMAIL_VERIFIED = "email_verified";
    private static final String PRIVATE_EMAIL = "is_private_email";

    public Map<String, Object> toOAuth2Attributes() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("sub", subject);
        if (StringUtils.hasText(email)) {
            attributes.put("email", email);
        }
        attributes.put(EMAIL_VERIFIED, emailVerified);
        attributes.put(PRIVATE_EMAIL, privateEmail);
        return Map.copyOf(attributes);
    }
}
