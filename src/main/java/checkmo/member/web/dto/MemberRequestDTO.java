package checkmo.member.web.dto;

import checkmo.member.internal.entity.MemberInterestCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class MemberProfileUpdate {
        @Size(max = 20, message = "소개는 20자 이하여야 합니다")
        private String description;

        private String imgUrl;

        private List<MemberInterestCategory> categories;
    }

    @Getter
    @NoArgsConstructor
    public static class PasswordUpdate {
        @NotBlank(message = "현재 비밀번호는 필수입니다")
        private String currentPassword;

        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Size(min = 6, max = 10, message = "비밀번호는 6-10자여야 합니다")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).*$", message = "비밀번호는 영어 및 특수문자를 포함해야 합니다")
        private String newPassword;
    }

    @Getter
    @NoArgsConstructor
    public static class AdditionalInfo {
        @NotBlank(message = "닉네임은 필수입니다")
        @Pattern(regexp = "^[a-z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]*$",
                message = "닉네임은 영어 소문자 및 특수문자만 사용 가능합니다")
        @Schema(description = "닉네임(영어 소문자 및 특수문자 최대 6자", example = "nick")
        private String nickname;

        @Size(max = 30, message = "소개는 30자 이하여야 합니다")
        private String description;

        private String imgUrl;

        @NotEmpty(message = "관심 카테고리는 최소 1개 이상 선택해야 합니다")
        private List<MemberInterestCategory> categories;
    }
}