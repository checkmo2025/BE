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
    CLUB_DUPLICATED_NAME(HttpStatus.CONFLICT, "CLUB_400", "이미 사용 중인 독서 모임 이름입니다."),
    CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_401", "독서 모임을 찾을 수 없습니다."),
    CLUB_SEARCH_KEYWORD_TOO_LONG(HttpStatus.BAD_REQUEST, "CLUB_402", "검색 키워드는 40자 이하로 입력해주세요."),

    // 클럽 회원
    CLUB_MEMBER_ALREADY_JOINED(HttpStatus.CONFLICT, "CLUB_MEMBER_400", "이미 가입된 회원입니다."),
    CLUB_MEMBER_NOT_IN_CLUB(HttpStatus.FORBIDDEN, "CLUB_MEMBER_401", "해당 클럽의 회원이 아닙니다."),
    CLUB_STAFF_ONLY(HttpStatus.FORBIDDEN, "CLUB_MEMBER_402", "독서 모임 운영진만 접근할 수 있습니다."),
    CLUB_MEMBER_INVALID_STATUS(HttpStatus.BAD_REQUEST, "CLUB_MEMBER_403", "클럽 회원의 상태가 유효하지 않습니다."),
    CLUB_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_MEMBER_404", "해당 클럽 회원을 찾을 수 없습니다."),
    CLUB_OWNER_CANNOT_LEAVE(HttpStatus.FORBIDDEN, "CLUB_MEMBER_405", "독서 모임 개설자는 탈퇴할 수 없습니다."),
    CLUB_MEMBER_IS_NOT_ACTIVE(HttpStatus.CONFLICT, "CLUB_MEMBER_406", "활성 회원(STAFF, MEMBER, OWNER)만 수행할 수 있습니다."),
    CLUB_OWNER_ONLY(HttpStatus.FORBIDDEN, "CLUB_MEMBER_407", "독서 모임 개설자만 접근할 수 있습니다."),
    CLUB_OWNER_CANNOT_BE_KICKED(HttpStatus.FORBIDDEN, "CLUB_MEMBER_408", "독서 모임 개설자는 강퇴할 수 없습니다."),
    CLUB_MEMBER_CANNOT_CHANGE_OWN_ROLE(HttpStatus.FORBIDDEN, "CLUB_MEMBER_409", "본인의 클럽 내 역할을 변경할 수 없습니다."),
    CLUB_OWNER_ROLE_CHANGE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "CLUB_MEMBER_410", "개설자의 역할은 변경할 수 없습니다. 개설자 위임을 사용하세요."),
    CLUB_OWNER_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_MEMBER_411", "독서 모임 개설자를 찾을 수 없습니다."),
    CLUB_PARTICIPANTS_JOIN_REQUIRED(HttpStatus.FORBIDDEN, "CLUB_MEMBER_412", "모임 회원은 가입 후에 조회 가능합니다"),
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
