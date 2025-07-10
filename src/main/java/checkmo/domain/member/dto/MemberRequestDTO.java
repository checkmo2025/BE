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
}