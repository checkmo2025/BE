package checkmo.infra.s3.internal.exception;

import checkmo.common.apiPayload.exception.GeneralException;

public class S3InfraException extends GeneralException {
    public S3InfraException(S3ErrorStatus status) {
        super(status);
    }

    public S3InfraException(S3ErrorStatus status, String detailMessage) {
        super(status, detailMessage);
    }
}
