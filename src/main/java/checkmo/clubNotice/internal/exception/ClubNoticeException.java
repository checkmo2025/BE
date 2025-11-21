package checkmo.clubNotice.internal.exception;

import checkmo.common.apiPayload.exception.GeneralException;

public class ClubNoticeException extends GeneralException {
    public ClubNoticeException(ClubNoticeErrorStatus status) {
        super(status);
    }
}
