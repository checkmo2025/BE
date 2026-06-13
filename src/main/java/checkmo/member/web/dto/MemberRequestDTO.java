package checkmo.member.web.dto;

import checkmo.member.internal.entity.MemberInterestCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class MemberRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class MemberProfileUpdate {
        @Size(max = 20, message = "소개는 20자 이하여야 합니다")
        private String description;

        private String imgUrl;

        @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "올바른 전화번호 형식이어야 합니다")
        private String phoneNumber;

        private List<MemberInterestCategory> categories;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdatePassword {
        @NotBlank(message = "현재 비밀번호는 필수입니다")
        private String currentPassword;

        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Size(min = 6, max = 24, message = "비밀번호는 6-24자여야 합니다")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).*$", message = "비밀번호는 영어 및 특수문자를 포함해야 합니다")
        private String newPassword;

        @NotBlank(message = "비밀번호 확인은 필수입니다")
        private String confirmPassword;
    }

    @Getter
    @NoArgsConstructor
    public static class AdditionalInfo {
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(max = 20, message = "닉네임은 최대 20자까지 가능합니다")
        @Pattern(regexp = "^[a-z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]*$",
                message = "닉네임은 영어 소문자 및 특수문자만 사용 가능합니다")
        @Schema(description = "닉네임(영어 소문자 및 특수문자 최대 20자)", example = "nick")
        private String nickname;

        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 10, message = "이름은 최대 10자까지 가능합니다")
        private String name;

        @NotBlank(message = "전화번호는 필수입니다")
        @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
                message = "올바른 전화번호 형식이어야 합니다 (예: 010-1234-5678)")
        @Schema(description = "전화번호(하이픈 포함)", example = "010-1234-5678")
        private String phoneNumber;

        @Size(max = 40, message = "소개는 40자 이하여야 합니다")
        private String description;

        private String imgUrl;

        @NotEmpty(message = "관심 카테고리는 최소 1개 이상 선택해야 합니다")
        @Size(min = 1, max = 6, message = "관심 카테고리는 1개에서 6개까지 선택 가능합니다")
        private List<MemberInterestCategory> categories;
    }

    @Getter
    @NoArgsConstructor
    public static class FindEmail {
        @NotBlank(message = "이름은 필수 입력 항목입니다.")
        private String name;

        @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
        private String phoneNumber;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateEmail {
        @NotBlank(message = "기존 이메일을 입력해주세요.")
        private String currentEmail;

        @NotBlank(message = "변경할 이메일을 입력해주세요.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        private String newEmail;

        @NotBlank(message = "인증번호를 입력해주세요.")
        private String verificationCode;
    }
}