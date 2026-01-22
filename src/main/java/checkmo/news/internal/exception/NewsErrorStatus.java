package checkmo.news.internal.exception;

import checkmo.common.apiPayload.code.BaseErrorCode;
import checkmo.common.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NewsErrorStatus implements BaseErrorCode {

    NEWS_NOT_FOUND(HttpStatus.NOT_FOUND, "NEWS_400", "소식을 찾을 수 없습니다."),
    NEWS_IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "NEWS_401", "소식 이미지는 최대 5개까지 등록 가능합니다."),
    NEWS_NOT_PUBLISHED(HttpStatus.NOT_FOUND, "NEWS_402", "현재 공개되지 않은 소식입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .httpStatus(httpStatus)
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }
}