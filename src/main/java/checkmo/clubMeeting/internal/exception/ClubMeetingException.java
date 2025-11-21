package checkmo.clubMeeting.internal.exception;

import checkmo.common.apiPayload.exception.GeneralException;

public class ClubMeetingException extends GeneralException {
    public ClubMeetingException(ClubMeetingErrorStatus status) {
        super(status);
    }
}
