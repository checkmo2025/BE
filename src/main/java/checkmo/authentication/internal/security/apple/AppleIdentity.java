package checkmo.authentication.internal.security.apple;

public record AppleIdentity(
        String subject,
        String providerId,
        String email,
        boolean emailVerified,
        boolean privateEmail,
        String audience
) {
}
