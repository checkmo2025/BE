package checkmo.realtime.internal.exception.general;

import checkmo.common.apiPayload.code.BaseErrorCode;
import checkmo.common.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RealtimeGeneralErrorStatus implements BaseErrorCode {
    MEETING_NOT_IN_CLUB(HttpStatus.BAD_REQUEST, "RTM_001", "모임이 클럽에 속하지 않습니다."),
    TEAM_NOT_IN_ClUB(HttpStatus.BAD_REQUEST, "RTM_002", "팀이 클럽에 속하지 않습니다."),
    NOT_ACTIVE_CLUB_MEMBER(HttpStatus.BAD_REQUEST, "RTM_003", "활동 중인 클럽 회원이 아닙니다."),
    NOT_TEAM_MEMBER_OR_STAFF(HttpStatus.BAD_REQUEST, "RTM_004", "팀 멤버 또는 운영진만 접근할 수 있습니다."),

    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "RTM_005", "메시지를 찾을 수 없습니다.");

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
