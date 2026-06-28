package checkmo.authentication.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class SignUp {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 형식이 아닙니다")
        @Schema(description = "이메일 주소", example = "test@example.com")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 6, max = 24, message = "비밀번호는 6-24자여야 합니다")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).*$", message = "비밀번호는 영어 및 특수문자를 포함해야 합니다")
        @Schema(description = "비밀번호(영어+특수문자 포함 6~24자)", example = "pass123!")
        private String password;

        @Schema(description = "약관 동의 목록. compatibility mode에서는 생략 가능합니다.")
        private List<@NotNull @Valid Agreement> agreements = List.of();
    }

    @Getter
    @NoArgsConstructor
    public static class Agreement {
        @NotNull(message = "약관 ID는 필수입니다.")
        @Schema(description = "약관 ID", example = "1")
        private Long termsId;

        @NotNull(message = "약관 동의 여부는 필수입니다.")
        @Schema(description = "동의 여부", example = "true")
        private Boolean agreed;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Login {
        @NotBlank(message = "이메일 또는 닉네임을 입력해주세요")
        private String identifier;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 6, max = 24, message = "비밀번호는 6-24자여야 합니다")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).*$", message = "비밀번호는 영어 및 특수문자를 포함해야 합니다")
        private String password;
    }

    @Getter
    @NoArgsConstructor
    public static class AppleAppLogin {
        @NotBlank(message = "Apple identityToken은 필수입니다")
        private String identityToken;

        @NotBlank(message = "Apple rawNonce는 필수입니다")
        private String rawNonce;

        private String authorizationCode;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailVerification {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "인증 코드는 필수입니다")
        private String verificationCode;
    }
}
