package checkmo.realtime.internal.exception.general;

import checkmo.common.apiPayload.exception.GeneralException;

public class RealtimeGeneralException extends GeneralException {
    public RealtimeGeneralException(RealtimeGeneralErrorStatus status) {
        super(status);
    }
}
