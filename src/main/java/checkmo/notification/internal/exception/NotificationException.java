package checkmo.notification.internal.exception;

import checkmo.common.apiPayload.exception.GeneralException;

public class NotificationException extends GeneralException {
    public NotificationException(NotificationErrorStatus status) {
        super(status);
    }
}
