package checkmo.book.internal.exception;

import checkmo.common.apiPayload.exception.GeneralException;

public class BookException extends GeneralException {
    public BookException(BookErrorStatus status) {
        super(status);
    }
}
