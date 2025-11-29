package checkmo.authentication.internal.exception;

import checkmo.common.apiPayload.exception.GeneralException;

public class AuthException extends GeneralException {
    public AuthException(AuthErrorStatus status) {
        super(status);
    }

    public AuthException(AuthErrorStatus status, String message) {
        super(status, message);
    }
}
