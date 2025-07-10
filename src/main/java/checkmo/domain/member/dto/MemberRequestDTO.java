package checkmo.domain.member.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class MemberRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class MemberProfileUpdateRequestDTO {
        @Size(max = 20, message = "소개는 20자 이하여야 합니다")
        private String description;

        private String imgUrl;

        private List<Long> categoryIds;
    }

    @Getter
    @NoArgsConstructor
    public static class PasswordUpdateRequestDTO {
        @NotBlank(message = "현재 비밀번호는 필수입니다")
        private String currentPassword;

        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Size(min = 6, max = 10, message = "비밀번호는 6-10자여야 합니다")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).*$", message = "비밀번호는 영어 및 특수문자를 포함해야 합니다")
        private String newPassword;
    }

    public static class EmailVerificationRequestDTO {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "인증 코드는 필수입니다")
        private String verificationCode;
    }

    public static class SignUpRequestDTO {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 6, max = 10, message = "비밀번호는 6-10자여야 합니다")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).*$", message = "비밀번호는 영어 및 특수문자를 포함해야 합니다")
        private String password;
    }

    @Getter
    @NoArgsConstructor
    public static class AdditionalInfoDTO {

        @NotBlank(message = "닉네임은 필수입니다")
        @Pattern(regexp = "^[a-z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]*$",
                message = "닉네임은 영어 소문자 및 특수문자만 사용 가능합니다")
        private String nickname;

        @Size(max = 30, message = "소개는 30자 이하여야 합니다")
        private String description;

        private String imgUrl;

        @NotEmpty(message = "관심 카테고리는 최소 1개 이상 선택해야 합니다")
        private List<Long> categoryIds;
    }
}