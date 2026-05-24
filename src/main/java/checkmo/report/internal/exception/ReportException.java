package checkmo.report.internal.exception;

import checkmo.common.apiPayload.exception.GeneralException;

public class ReportException extends GeneralException {

    public ReportException(ReportErrorStatus status) {
        super(status);
    }
}