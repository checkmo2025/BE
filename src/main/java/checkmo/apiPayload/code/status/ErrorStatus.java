package checkmo.apiPayload.code.status;

import checkmo.apiPayload.code.BaseErrorCode;
import checkmo.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "금지된 요청입니다."),

    // 책
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_404", "책을 찾을 수 없습니다."),

    //책이야기
    BOOK_STORY_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_STORY_404", "책 이야기를 찾을 수 없습니다."),
    BOOK_STORY_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "BOOK_STORY_405", "책 이야기 수정/삭제 권한이 없습니다."),

    // 댓글
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_404", "댓글을 찾을 수 없습니다."),
    INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "COMMENT_401", "부모 댓글이 해당 책 이야기에 속하지 않습니다."),
    COMMENT_DEPTH_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "COMMENT_402", "댓글은 2단계까지만 허용됩니다."),

    // 모임
    CLUB_DUPLICATED_NAME(HttpStatus.BAD_REQUEST, "CLUB_400", "이미 존재하는 독서클럽 이름입니다."),
    CLUB_INVALID_TAG_TYPE(HttpStatus.BAD_REQUEST, "CLUB_401", "유효하지 않은 공지 유형입니다. (공지, 모임, 투표 중 하나)"),
    CLUB_MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "CLUB_402", "이미 존재하는 클럽 회원입니다."),
    CLUB_MEMBER_ONLY(HttpStatus.FORBIDDEN, "CLUB_403", "해당 클럽의 회원이 아닙니다."),
    CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_404", "독서클럽을 찾을 수 없습니다."),
    CLUB_STAFF_ONLY(HttpStatus.FORBIDDEN, "CLUB_405", "독서클럽 운영진만 접근할 수 있습니다."),
    CLUB_BOOK_RECOMMEND_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_406", "추천 책을 찾을 수 없습니다."),
    CLUB_BOOK_RECOMMEND_FORBIDDEN(HttpStatus.FORBIDDEN, "CLUB_407", "해당 추천 책에 대한 권한이 없습니다."),
    CLUB_MEMBER_INVALID_STATUS(HttpStatus.BAD_REQUEST, "CLUB_408", "유효하지 않은 상태입니다."),
    CLUB_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_409", "해당 클럽 회원을 찾을 수 없습니다."),
    CLUB_STAFF_CANNOT_LEAVE(HttpStatus.FORBIDDEN, "CLUB_410", "운영진은 클럽을 탈퇴할 수 없습니다."),
    CLUB_MEMBER_IS_NOT_ACTIVE(HttpStatus.FORBIDDEN, "CLUB_411", "해당 클럽 회원은 활성화 상태(STAFF, MEMBER)가 아닙니다."),

    // 공지사항
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE_400", "공지사항을 찾을 수 없습니다."),
    NOTICE_MEETING_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "NOTICE_405", "모임 공지사항은 삭제할 수 없습니다."),

    // 투표
    MULTIPLE_SELECTION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "VOTE_401", "하나의 항목에만 투표 가능합니다."),
    VOTE_TIME_EXPIRED(HttpStatus.BAD_REQUEST, "VOTE_403", "투표 가능 시간이 아닙니다."),
    VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "VOTE_404", "투표를 찾을 수 없습니다."),

    // 이메일
    EMAIL_VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_401", "인증번호가 만료되었습니다."),
    EMAIL_VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_402", "잘못된 인증번호입니다."),
    EMAIL_VERIFICATION_CODE_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_403", "이미 인증된 이메일입니다."),
    EMAIL_VERIFICATION_CODE_ALREADY_SENT(HttpStatus.BAD_REQUEST, "EMAIL_404", "이미 인증번호가 발송되었습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_405", "이메일 인증이 필요합니다."),

    // 미팅
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_404", "독서모임을 찾을 수 없습니다."),

    // 팀
    TEAM_NUMBER_DUPLICATED_REQUEST(HttpStatus.BAD_REQUEST, "TEAM_401", "중복된 팀 번호가 요청되었습니다."),
    TEAM_MEETING_REQUIRED(HttpStatus.BAD_REQUEST, "TEAM_402", "팀은 반드시 독서모임에 속해야 합니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_404", "팀을 찾을 수 없습니다."),

    // 카테고리
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_404", "카테고리를 찾을 수 없습니다."),

    // 회원
    MEMBER_INACTIVE(HttpStatus.FORBIDDEN, "MEMBER_401", "비활성화된 회원입니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MEMBER_402", "이미 존재하는 회원입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404", "해당 회원을 찾을 수 없습니다."),
    MEMBER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "MEMBER_403", "인증되지 않은 회원입니다."),
    MEMBER_PROFILE_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "MEMBER_406", "이미 프로필이 완성된 회원입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MEMBER_407", "이미 존재하는 닉네임입니다."),
    MEMBER_PROFILE_NOT_COMPLETED(HttpStatus.FORBIDDEN, "MEMBER_408", "프로필이 완성되지 않은 회원입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "MEMBER_409", "이메일 또는 비밀번호가 일치하지 않습니다."),
    MEMBER_CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "MEMBER_410", "자기 자신을 팔로잉할 수 없습니다."),
    MEMBER_ALREADY_FOLLOWING(HttpStatus.BAD_REQUEST, "MEMBER_411", "이미 팔로잉 중인 회원입니다."),
    MEMBER_NOT_FOLLOWING(HttpStatus.BAD_REQUEST, "MEMBER_412", "팔로우 중이지 않은 회원입니다."),
    MEMBER_NOT_FOLLOWER(HttpStatus.BAD_REQUEST, "MEMBER_413", "팔로워가 아닌 회원입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER_500", "서버 내부 오류입니다. 관리자에게 문의 바랍니다."),

    // 발제
    TOPIC_MEETING_REQUIRED(HttpStatus.BAD_REQUEST, "TOPIC_401", "발제는 반드시 독서모임에 속해야 합니다."),
    TOPIC_CLUB_MEMBER_REQUIRED(HttpStatus.BAD_REQUEST, "TOPIC_402", "발제는 반드시 독서클럽 회원이 작성해야 합니다."),
    TOPIC_FORBIDDEN(HttpStatus.FORBIDDEN, "TOPIC_403", "해당 발제에 대한 권한이 없습니다."),
    TOPIC_NOT_FOUND(HttpStatus.NOT_FOUND, "TOPIC_404", "발제를 찾을 수 없습니다."),

    // 한줄평
    BOOK_REVIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "BOOK_REVIEW_403", "이 한줄평에 대한 수정/삭제 권한이 없습니다."),
    BOOK_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_REVIEW_404", "한줄평을 찾을 수 없습니다."),

    //알림
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_401", "알림을 찾을 수 없습니다."),
    NOTIFICATION_ALREADY_READ(HttpStatus.BAD_REQUEST, "NOTIFICATION_402", "이미 읽은 알림입니다."),
    NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "NOTIFICATION_403", "해당 알림에 접근할 권한이 없습니다."),

    // 페이지네이션 파라미터
    CURSOR_ID_POSITIVE(HttpStatus.BAD_REQUEST, "PAGINATION_401", "커서 값은 양수여야 합니다."),
    SIZE_POSITIVE(HttpStatus.BAD_REQUEST, "PAGINATION_402", "조회할 개수는 양수여야 합니다."),

    // S3
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "S3_400", "지원하지 않는 파일 형식입니다. (이미지 파일만 허용됩니다.)"),
    S3_FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500", "S3 파일 삭제에 실패했습니다."),
    S3_PRESIGNED_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_501", "Presigned URL 생성에 실패했습니다."),
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
