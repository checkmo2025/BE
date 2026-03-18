package checkmo.clubMeeting.internal.exception;

import checkmo.common.apiPayload.code.BaseErrorCode;
import checkmo.common.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ClubMeetingErrorStatus implements BaseErrorCode {

    // 발제
    TOPIC_MEETING_REQUIRED(HttpStatus.BAD_REQUEST, "TOPIC_401", "발제는 반드시 정기모임에 속해야 합니다."),
    TOPIC_CLUB_MEMBER_REQUIRED(HttpStatus.BAD_REQUEST, "TOPIC_402", "발제는 반드시 독서클럽 회원이 작성해야 합니다."),
    TOPIC_FORBIDDEN(HttpStatus.FORBIDDEN, "TOPIC_403", "해당 발제에 대한 권한이 없습니다."),
    TOPIC_NOT_FOUND(HttpStatus.NOT_FOUND, "TOPIC_404", "발제를 찾을 수 없습니다."),

    // 한줄평
    BOOK_REVIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "BOOK_REVIEW_403", "이 한줄평에 대한 수정/삭제 권한이 없습니다."),
    BOOK_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_REVIEW_404", "한줄평을 찾을 수 없습니다."),

    // 팀
    TEAM_NUMBER_DUPLICATED_REQUEST(HttpStatus.BAD_REQUEST, "TEAM_401", "중복된 팀 번호가 요청되었습니다."),
    TEAM_MEETING_REQUIRED(HttpStatus.BAD_REQUEST, "TEAM_402", "팀은 반드시 정기모임에 속해야 합니다."),
    NOT_TEAM_MEMBER_OR_STAFF(HttpStatus.FORBIDDEN, "TEAM_403", "팀 멤버 또는 운영진만 접근할 수 있습니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_404", "팀을 찾을 수 없습니다."),

    // 미팅
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_401", "해당 정기모임을 찾을 수 없습니다."),
    NEXT_MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_402", "다음 정기모임이 존재하지 않습니다."),

    // 페이지네이션 파라미터
    CURSOR_ID_POSITIVE(HttpStatus.BAD_REQUEST, "PAGINATION_401", "커서 값은 양수여야 합니다."),

    // 클럽 멤버 권한
    CLUB_STAFF_ONLY(HttpStatus.FORBIDDEN, "CLUB_MEETING_403", "독서클럽 운영진만 접근할 수 있습니다."),
    INACTIVE_CLUB_MEMBER(HttpStatus.FORBIDDEN, "CLUB_MEETING_404", "활동 중인 독서클럽 회원만 접근할 수 있습니다.");

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
