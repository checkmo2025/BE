package checkmo.clubNotice.internal.exception;

import checkmo.common.apiPayload.code.BaseErrorCode;
import checkmo.common.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ClubNoticeErrorStatus implements BaseErrorCode {
    // 공지사항
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE_400", "공지사항을 찾을 수 없습니다."),
    MEETING_NOT_IN_CLUB(HttpStatus.BAD_REQUEST, "NOTICE_401", "공지사항의 모임이 해당 동아리에 속해있지 않습니다."),
    PINNED_NOTICE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "NOTICE_402", "고정 공지사항은 최대 5개까지 설정할 수 있습니다."),
    NOTICE_EMPTY(HttpStatus.NOT_FOUND, "NOTICE_403", "공지사항이 존재하지 않습니다."),
    CLUB_MEMBER_INACTIVE(HttpStatus.FORBIDDEN, "NOTICE_404", "동아리 회원이 활동 상태가 아닙니다."),

    // 투표
    INSUFFICIENT_VOTE_ITEMS(HttpStatus.BAD_REQUEST, "VOTE_400", "투표 항목이 2개 미만입니다."),
    MULTIPLE_SELECTION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "VOTE_401", "하나의 항목에만 투표 가능합니다."),
    VOTE_ITEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "VOTE_402", "선택한 항목이 존재하지 않습니다."),
    VOTE_TIME_INVALID(HttpStatus.BAD_REQUEST, "VOTE_403", "투표 가능 시간이 아닙니다."),
    VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "VOTE_404", "투표를 찾을 수 없습니다."),
    VOTE_PERIOD_REQUIRED(HttpStatus.BAD_REQUEST, "VOTE_405", "투표 시작/마감 시간은 필수입니다."),
    VOTE_START_AFTER_DEADLINE(HttpStatus.BAD_REQUEST, "VOTE_406", "투표 시작 시간은 마감 시간 이전이어야 합니다."),
    VOTE_ALREADY_EXISTS(HttpStatus.CONFLICT, "VOTE_407", "이미 공지사항에 투표가 연결되어 있습니다."),

    // 댓글
    NOTICE_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE_COMMENT_400", "공지사항 댓글을 찾을 수 없습니다."),
    NOTICE_COMMENT_UNAUTHORIZED(HttpStatus.FORBIDDEN, "NOTICE_COMMENT_401", "공지사항 댓글 수정/삭제 권한이 없습니다."),

    // 이미지
    NOTICE_IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "NOTICE_IMAGE_400", "공지사항 이미지 최대 업로드 개수를 초과했습니다."),
    NOTICE_COMMENT_IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "NOTICE_COMMENT_IMAGE_400", "공지사항 댓글 이미지는 최대 5개까지 업로드할 수 있습니다."),
    NOTICE_COMMENT_IMAGE_INVALID(HttpStatus.BAD_REQUEST, "NOTICE_COMMENT_IMAGE_401", "본인이 업로드한 공지사항 댓글 이미지만 첨부할 수 있습니다.");
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
