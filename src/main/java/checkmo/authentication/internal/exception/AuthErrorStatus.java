package checkmo.authentication.internal.exception;

import checkmo.common.apiPayload.code.BaseErrorCode;
import checkmo.common.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorStatus implements BaseErrorCode {

    // 이메일
    EMAIL_VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_400", "인증번호가 만료되었습니다."),
    EMAIL_VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_401", "잘못된 인증번호입니다."),
    EMAIL_VERIFICATION_CODE_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_402", "이미 인증된 이메일입니다."),
    EMAIL_VERIFICATION_CODE_ALREADY_SENT(HttpStatus.BAD_REQUEST, "EMAIL_403", "이미 인증번호가 발송되었습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_404", "이메일 인증이 필요합니다."),

    // AUTH
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_400", "인증된 회원을 찾을 수 없습니다."),
    MEMBER_INACTIVE(HttpStatus.FORBIDDEN, "AUTH_401", "비활성화된 회원입니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "AUTH_402", "이미 존재하는 회원입니다."),
    MEMBER_PROFILE_NOT_COMPLETED(HttpStatus.FORBIDDEN, "AUTH_403", "프로필이 완성되지 않은 회원입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_404", "이메일 또는 비밀번호가 일치하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_500", "서버 내부 오류입니다. 관리자에게 문의 바랍니다."),
    GHOST_MEMBER_CLEANED_UP(HttpStatus.UNAUTHORIZED, "AUTH_405", "추가정보 입력 제한 시간이 초과되어 초기화되었습니다. 다시 가입해주세요."),
    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "AUTH_406", "로그아웃된 토큰입니다. 다시 로그인해주세요."),
    SOCIAL_MEMBER_CANNOT_CHANGE_EMAIL(HttpStatus.BAD_REQUEST, "AUTH_407", "소셜 로그인 회원은 이메일을 변경할 수 없습니다."),
    CURRENT_EMAIL_INCORRECT(HttpStatus.BAD_REQUEST, "AUTH_408", "입력하신 기존 이메일 정보가 정확하지 않습니다."),
    PASSWORD_SAME_AS_OLD(HttpStatus.BAD_REQUEST, "AUTH_409", "새 비밀번호가 기존 비밀번호와 동일합니다."),
    NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH_410", "닉네임은 필수입니다."),
    SIGNUP_INCOMPLETE(HttpStatus.CONFLICT, "AUTH_411", "이미 가입 진행 중인 이메일입니다. 로그인 후 프로필을 완성해주세요."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_412", "유효하지 않은 리프레시 토큰입니다. 다시 로그인해주세요."),
    APPLE_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH_413", "Apple 최초 로그인에는 이메일 정보가 필요합니다."),
    SOCIAL_ACCOUNT_EMAIL_CONFLICT(HttpStatus.CONFLICT, "AUTH_414", "이미 다른 계정으로 가입된 이메일입니다."),
    APPLE_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_415", "유효하지 않은 Apple 인증 정보입니다.")
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
                .httpStatus(httpStatus)
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }
}
