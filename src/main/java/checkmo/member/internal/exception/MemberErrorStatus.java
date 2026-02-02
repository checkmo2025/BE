package checkmo.member.internal.exception;

import checkmo.common.apiPayload.code.BaseErrorCode;
import checkmo.common.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorStatus implements BaseErrorCode {
    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_400", "해당 회원을 찾을 수 없습니다."),

    // 팔로우
    MEMBER_CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "FOLLOW_400", "자기 자신을 팔로잉할 수 없습니다."),
    MEMBER_ALREADY_FOLLOWING(HttpStatus.BAD_REQUEST, "FOLLOW_401", "이미 팔로잉 중인 회원입니다."),
    MEMBER_NOT_FOLLOWING(HttpStatus.BAD_REQUEST, "FOLLOW_402", "팔로우 중이지 않은 회원입니다."),
    MEMBER_NOT_FOLLOWER(HttpStatus.BAD_REQUEST, "FOLLOW_403", "팔로워가 아닌 회원입니다."),
    MULTIPLE_ACCOUNTS_FOUND(HttpStatus.BAD_REQUEST, "MEMBER_409", "해당 정보로 가입된 계정이 여러 개입니다. 관리자에게 문의해주세요."),

    // 신고
    CANNOT_REPORT_SELF(HttpStatus.BAD_REQUEST, "REPORT_400", "자기 자신을 신고할 수 없습니다.")
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
