package checkmo.clubManagement.internal.excepetion;

import checkmo.common.apiPayload.code.BaseErrorCode;
import checkmo.common.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ClubManagementErrorStatus implements BaseErrorCode {
    // 클럽
    CLUB_DUPLICATED_NAME(HttpStatus.BAD_REQUEST, "CLUB_400", "이미 존재하는 독서클럽 이름입니다."),
    CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_401", "독서클럽을 찾을 수 없습니다."),

    // 클럽 회원
    CLUB_MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "CLUB_MEMBER_400", "이미 존재하는 클럽 회원입니다."),
    CLUB_MEMBER_ONLY(HttpStatus.FORBIDDEN, "CLUB_MEMBER_401", "해당 클럽의 회원이 아닙니다."),
    CLUB_STAFF_ONLY(HttpStatus.FORBIDDEN, "CLUB_MEMBER_402", "독서클럽 운영진만 접근할 수 있습니다."),
    CLUB_MEMBER_INVALID_STATUS(HttpStatus.BAD_REQUEST, "CLUB_403", "유효하지 않은 상태입니다."),
    CLUB_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_MEMBER_404", "해당 클럽 회원을 찾을 수 없습니다."),
    CLUB_STAFF_CANNOT_LEAVE(HttpStatus.FORBIDDEN, "CLUB_MEMBER_405", "운영진은 클럽을 탈퇴할 수 없습니다."),
    CLUB_MEMBER_IS_NOT_ACTIVE(HttpStatus.FORBIDDEN, "CLUB_MEMBER_406", "해당 클럽 회원은 활성화 상태(STAFF, MEMBER)가 아닙니다."),

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
