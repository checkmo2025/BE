package checkmo.common.apiPayload.code;

public interface BaseErrorCode {

    ErrorReasonDTO getReason();

    ErrorReasonDTO getReasonHttpStatus();
}
