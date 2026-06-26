package checkmo.authentication.internal.security.apple;

public class InvalidAppleIdentityTokenException extends RuntimeException {

    private static final String MESSAGE = "Invalid Apple identity token";

    public InvalidAppleIdentityTokenException() {
        super(MESSAGE);
    }

    public InvalidAppleIdentityTokenException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
