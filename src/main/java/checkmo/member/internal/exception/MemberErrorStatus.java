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
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "MEMBER_410", "비밀번호가 일치하지 않습니다."),
    CURRENT_PASSWORD_INCORRECT(HttpStatus.BAD_REQUEST, "MEMBER_412", "기존 비밀번호가 올바르지 않습니다. 다시 시도해주세요."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MEMBER_413", "이미 사용 중인 이메일입니다."),
    CURRENT_EMAIL_INCORRECT(HttpStatus.BAD_REQUEST, "MEMBER_414", "입력하신 기존 이메일 정보가 정확하지 않습니다."),
    NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "MEMBER_415", "닉네임은 필수입니다."),
    TERMS_NOT_FOUND(HttpStatus.BAD_REQUEST, "TERMS_400", "해당 약관을 찾을 수 없습니다."),
    REQUIRED_TERMS_CANNOT_BE_DISAGREED(HttpStatus.BAD_REQUEST, "TERMS_401", "필수 약관은 동의가 필요합니다."),
    DUPLICATE_TERMS_AGREEMENT(HttpStatus.BAD_REQUEST, "TERMS_402", "동일한 약관을 중복 제출할 수 없습니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "TERMS_403", "필수 약관 동의가 필요합니다."),
    DUPLICATE_ACTIVE_TERMS_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "TERMS_500", "활성 약관 종류가 중복되어 있습니다."),

    // 팔로우
    MEMBER_CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "FOLLOW_400", "자기 자신을 팔로잉할 수 없습니다."),
    MEMBER_ALREADY_FOLLOWING(HttpStatus.BAD_REQUEST, "FOLLOW_401", "이미 팔로잉 중인 회원입니다."),
    MEMBER_NOT_FOLLOWING(HttpStatus.BAD_REQUEST, "FOLLOW_402", "팔로우 중이지 않은 회원입니다."),
    MEMBER_NOT_FOLLOWER(HttpStatus.BAD_REQUEST, "FOLLOW_403", "팔로워가 아닌 회원입니다."),
    MULTIPLE_ACCOUNTS_FOUND(HttpStatus.BAD_REQUEST, "MEMBER_409", "해당 정보로 가입된 계정이 여러 개입니다. 관리자에게 문의해주세요."),

    // 차단
    MEMBER_CANNOT_BLOCK_SELF(HttpStatus.BAD_REQUEST, "BLOCK_400", "자기 자신을 차단할 수 없습니다."),
    MEMBER_ALREADY_BLOCKED(HttpStatus.BAD_REQUEST, "BLOCK_401", "이미 차단한 회원입니다."),
    MEMBER_BLOCK_NOT_FOUND(HttpStatus.BAD_REQUEST, "BLOCK_402", "차단한 회원이 아닙니다."),
    MEMBER_BLOCKED_RELATION(HttpStatus.BAD_REQUEST, "BLOCK_403", "차단 관계가 있는 회원은 팔로우할 수 없습니다."),
    MEMBER_BLOCKED_BY_ME(HttpStatus.FORBIDDEN, "BLOCK_404", "차단한 사용자입니다."),
    MEMBER_BLOCKED_ME(HttpStatus.FORBIDDEN, "BLOCK_405", "조회가 불가능한 프로필 입니다."),
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
