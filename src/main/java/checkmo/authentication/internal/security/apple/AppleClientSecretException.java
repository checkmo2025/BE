package checkmo.authentication.internal.security.apple;

public class AppleClientSecretException extends RuntimeException {

    public AppleClientSecretException(String message) {
        super(message);
    }

    public AppleClientSecretException(String message, Throwable cause) {
        super(message, cause);
    }
}
