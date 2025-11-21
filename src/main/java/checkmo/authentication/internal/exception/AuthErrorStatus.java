package checkmo.authentication.internal.exception;

import checkmo.common.apiPayload.code.BaseErrorCode;
import checkmo.common.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorStatus implements BaseErrorCode {

    // 이메일
    EMAIL_VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_400", "인증번호가 만료되었습니다."),
    EMAIL_VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_401", "잘못된 인증번호입니다."),
    EMAIL_VERIFICATION_CODE_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_402", "이미 인증된 이메일입니다."),
    EMAIL_VERIFICATION_CODE_ALREADY_SENT(HttpStatus.BAD_REQUEST, "EMAIL_403", "이미 인증번호가 발송되었습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_404", "이메일 인증이 필요합니다."),

    // AUTH
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_400", "인증된 회원을 찾을 수 없습니다."),
    MEMBER_INACTIVE(HttpStatus.FORBIDDEN, "AUTH_401", "비활성화된 회원입니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "AUTH_402", "이미 존재하는 회원입니다."),
    MEMBER_PROFILE_NOT_COMPLETED(HttpStatus.FORBIDDEN, "AUTH_403", "프로필이 완성되지 않은 회원입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_404", "이메일 또는 비밀번호가 일치하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_500", "서버 내부 오류입니다. 관리자에게 문의 바랍니다."),
    ;

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
