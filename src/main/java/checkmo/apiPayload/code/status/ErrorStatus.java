package checkmo.apiPayload.code.status;

import checkmo.apiPayload.code.BaseErrorCode;
import checkmo.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // For test
    TEMP_EXCEPTION(HttpStatus.BAD_REQUEST, "TEMP4001", "이거는 테스트"),

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK404", "책을 찾을 수 없습니다."),

    EMAIL_VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_401", "인증번호가 만료되었습니다."),
    EMAIL_VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_402", "잘못된 인증번호입니다."),
    EMAIL_VERIFICATION_CODE_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_403", "이미 인증된 이메일입니다."),
    EMAIL_VERIFICATION_CODE_ALREADY_SENT(HttpStatus.BAD_REQUEST, "EMAIL_404", "이미 인증번호가 발송되었습니다.");

    CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB404", "독서클럽을 찾을 수 없습니다."),

    CLUB_MEMBER_ONLY(HttpStatus.FORBIDDEN, "CLUB_MEMBER403", "해당 클럽의 회원이 아닙니다."),
    CLUB_STAFF_ONLY(HttpStatus.FORBIDDEN, "CLUB_STAFF403", "독서클럽 운영진만 접근할 수 있습니다."),

    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING404", "독서모임을 찾을 수 없습니다."),
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
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
