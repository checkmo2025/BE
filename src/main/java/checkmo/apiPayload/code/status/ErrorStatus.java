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
    TEMP_EXCEPTION(HttpStatus.BAD_REQUEST, "TEMP_4001", "이거는 테스트"),

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "금지된 요청입니다."),

    // 책
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_4004", "책을 찾을 수 없습니다."),

    // 모임
    CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_4004", "독서클럽을 찾을 수 없습니다."),
    CLUB_DUPLICATED_NAME(HttpStatus.BAD_REQUEST, "CLUB_4001", "이미 존재하는 독서클럽 이름입니다."),
    CLUB_MEMBER_ONLY(HttpStatus.FORBIDDEN, "CLUB_4002", "해당 클럽의 회원이 아닙니다."),
    CLUB_STAFF_ONLY(HttpStatus.FORBIDDEN, "CLUB_4005", "독서클럽 운영진만 접근할 수 있습니다."),
    CLUB_BOOK_RECOMMEND_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_4014", "추천 책을 찾을 수 없습니다."),
    CLUB_BOOK_RECOMMEND_FORBIDDEN(HttpStatus.FORBIDDEN, "CLUB_4015", "해당 추천 책에 대한 권한이 없습니다."),

    // 회원  
    EMAIL_VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_401", "인증번호가 만료되었습니다."),
    EMAIL_VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_402", "잘못된 인증번호입니다."),
    EMAIL_VERIFICATION_CODE_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_403", "이미 인증된 이메일입니다."),
    EMAIL_VERIFICATION_CODE_ALREADY_SENT(HttpStatus.BAD_REQUEST, "EMAIL_404", "이미 인증번호가 발송되었습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_4004", "회원 정보를 찾을 수 없습니다."),

    // 미팅
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_4004", "독서모임을 찾을 수 없습니다."),

    // 카테고리
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_4004", "카테고리를 찾을 수 없습니다."),

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
