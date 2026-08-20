package checkmo.chatbot.internal.exception;

import checkmo.common.apiPayload.code.BaseErrorCode;
import checkmo.common.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ChatbotErrorStatus implements BaseErrorCode {

    GEMINI_API_CALL_FAILED(HttpStatus.BAD_GATEWAY, "CHATBOT_502", "챗봇 응답 생성에 실패했습니다."),
    GEMINI_EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, "CHATBOT_503", "챗봇 응답이 비어 있습니다."),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATBOT_404", "존재하지 않는 챗봇 세션입니다.");

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
