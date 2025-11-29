package checkmo.clubManagement.internal.excepetion;

import checkmo.common.apiPayload.exception.GeneralException;

public class ClubManagementException extends GeneralException {
    public ClubManagementException(ClubManagementErrorStatus status) {
        super(status);
    }
}
