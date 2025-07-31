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

    //책이야기
    BOOK_STORY_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_STORY_4004", "책 이야기를 찾을 수 없습니다."),
    BOOK_STORY_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "BOOK_STORY_4005", "책 이야기 수정/삭제 권한이 없습니다."),

    // 모임
    CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_4004", "독서클럽을 찾을 수 없습니다."),
    CLUB_DUPLICATED_NAME(HttpStatus.BAD_REQUEST, "CLUB_4001", "이미 존재하는 독서클럽 이름입니다."),
    CLUB_MEMBER_ONLY(HttpStatus.FORBIDDEN, "CLUB_4002", "해당 클럽의 회원이 아닙니다."),
    CLUB_STAFF_ONLY(HttpStatus.FORBIDDEN, "CLUB_4005", "독서클럽 운영진만 접근할 수 있습니다."),
    CLUB_BOOK_RECOMMEND_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_4014", "추천 책을 찾을 수 없습니다."),
    CLUB_BOOK_RECOMMEND_FORBIDDEN(HttpStatus.FORBIDDEN, "CLUB_4015", "해당 추천 책에 대한 권한이 없습니다."),
    CLUB_INVALID_TAG_TYPE(HttpStatus.BAD_REQUEST, "CLUB_4011", "유효하지 않은 공지 유형입니다. (공지, 모임, 투표 중 하나)"),

    // 공지사항
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE_4004", "공지사항을 찾을 수 없습니다."),

    // 투표
    VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "VOTE_4004", "투표를 찾을 수 없습니다."),
    VOTE_TIME_EXPIRED(HttpStatus.BAD_REQUEST, "VOTE_4003", "투표 가능 시간이 아닙니다."),
    MULTIPLE_SELECTION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "VOTE_4001", "하나의 항목에만 투표 가능합니다."),

    // 이메일
    EMAIL_VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_401", "인증번호가 만료되었습니다."),
    EMAIL_VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_402", "잘못된 인증번호입니다."),
    EMAIL_VERIFICATION_CODE_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_403", "이미 인증된 이메일입니다."),
    EMAIL_VERIFICATION_CODE_ALREADY_SENT(HttpStatus.BAD_REQUEST, "EMAIL_404", "이미 인증번호가 발송되었습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_405", "이메일 인증이 필요합니다."),

    // 미팅
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_4004", "독서모임을 찾을 수 없습니다."),

    // 카테고리
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_4004", "카테고리를 찾을 수 없습니다."),

    // 회원
    MEMBER_INACTIVE(HttpStatus.FORBIDDEN, "MEMBER_401", "비활성화된 회원입니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MEMBER402", "이미 존재하는 회원입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404", "해당 회원을 찾을 수 없습니다."),
    MEMBER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "MEMBER_403", "인증되지 않은 회원입니다."),
    MEMBER_PROFILE_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "MEMBER_406", "이미 프로필이 완성된 회원입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MEMBER_407", "이미 존재하는 닉네임입니다."),
    MEMBER_PROFILE_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "MEMBER_408", "프로필이 완성되지 않은 회원입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "MEMBER_409", "이메일 또는 비밀번호가 일치하지 않습니다."),

    // 한줄평
    BOOK_REVIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "BOOK_REVIEW403", "이 한줄평에 대한 수정/삭제 권한이 없습니다."),
    BOOK_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_REVIEW404", "한줄평을 찾을 수 없습니다."),

    //알림
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION4001", "알림을 찾을 수 없습니다."),
    NOTIFICATION_ALREADY_READ(HttpStatus.BAD_REQUEST, "NOTIFICATION4002", "이미 읽은 알림입니다."),
    NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "NOTIFICATION4003", "해당 알림에 접근할 권한이 없습니다.");

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
