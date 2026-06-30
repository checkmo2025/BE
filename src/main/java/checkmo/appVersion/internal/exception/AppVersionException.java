package checkmo.appVersion.internal.exception;

import checkmo.common.apiPayload.exception.GeneralException;

public class AppVersionException extends GeneralException {
    public AppVersionException(AppVersionErrorStatus errorCode) {
        super(errorCode);
    }
}
