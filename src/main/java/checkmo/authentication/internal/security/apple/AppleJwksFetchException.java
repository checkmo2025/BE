package checkmo.authentication.internal.security.apple;

class AppleJwksFetchException extends RuntimeException {

    AppleJwksFetchException(String message) {
        super(message);
    }

    AppleJwksFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
