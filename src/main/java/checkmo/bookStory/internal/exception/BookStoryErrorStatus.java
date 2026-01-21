package checkmo.bookStory.internal.exception;

import checkmo.common.apiPayload.code.BaseErrorCode;
import checkmo.common.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BookStoryErrorStatus implements BaseErrorCode {
    //책이야기
    BOOK_STORY_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_STORY_404", "책 이야기를 찾을 수 없습니다."),
    BOOK_STORY_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "BOOK_STORY_405", "책 이야기 수정/삭제 권한이 없습니다."),

    // 책 이야기 댓글
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_404", "댓글을 찾을 수 없습니다."),
    COMMENT_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "COMMENT_403", "댓글 수정/삭제 권한이 없습니다."),
    INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "COMMENT_401", "부모 댓글이 해당 책 이야기에 속하지 않습니다."),
    COMMENT_DEPTH_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "COMMENT_402", "댓글은 2단계까지만 허용됩니다."),

    //
    CLUB_ACCESS_DENIED(HttpStatus.FORBIDDEN, "BOOK_STORY_403", "해당 독서클럽에 접근할 수 없습니다.");

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
