package checkmo.infra.s3.internal.exception;

import checkmo.common.apiPayload.code.BaseErrorCode;
import checkmo.common.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum S3ErrorStatus implements BaseErrorCode {

    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "S3_400", "지원하지 않는 파일 형식입니다. (이미지 파일만 허용됩니다.)"),
    S3_FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500", "S3 파일 삭제에 실패했습니다."),
    S3_PRESIGNED_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_501", "Presigned URL 생성에 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .code(code)
                .message(message)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .httpStatus(httpStatus)
                .code(code)
                .message(message)
                .isSuccess(false)
                .build();
    }
}
