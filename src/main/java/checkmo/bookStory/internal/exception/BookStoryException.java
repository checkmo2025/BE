package checkmo.bookStory.internal.exception;

import checkmo.common.apiPayload.exception.GeneralException;

public class BookStoryException extends GeneralException {
    public BookStoryException(BookStoryErrorStatus status) {
        super(status);
    }
}
