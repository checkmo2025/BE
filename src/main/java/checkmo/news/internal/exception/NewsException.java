package checkmo.news.internal.exception;

import checkmo.common.apiPayload.code.BaseErrorCode;
import checkmo.common.apiPayload.exception.GeneralException;

public class NewsException extends GeneralException {
    public NewsException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
