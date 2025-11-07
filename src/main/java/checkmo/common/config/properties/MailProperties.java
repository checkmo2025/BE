package checkmo.common.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    @Valid
    private Smtp smtp = new Smtp();

    @Valid
    private Auth auth = new Auth();

    @Getter
    @Setter
    public static class Smtp {

        @NotBlank(message = "SMTP 호스트는 필수입니다")
        private String host = "smtp.gmail.com";

        @Positive(message = "포트는 양수여야 합니다")
        @Min(value = 1, message = "포트는 1 이상이어야 합니다")
        @Max(value = 65535, message = "포트는 65535 이하여야 합니다")
        private int port = 587;

        @Positive(message = "타임아웃은 양수여야 합니다")
        @Min(value = 1000, message = "타임아웃은 1000ms 이상이어야 합니다")
        private int timeout = 5000;

        @Positive(message = "연결 타임아웃은 양수여야 합니다")
        @Min(value = 1000, message = "연결 타임아웃은 1000ms 이상이어야 합니다")
        private int connectionTimeout = 5000;

        @Positive(message = "쓰기 타임아웃은 양수여야 합니다")
        @Min(value = 1000, message = "쓰기 타임아웃은 1000ms 이상이어야 합니다")
        private int writeTimeout = 5000;

        private boolean starttlsEnable = true;
    }

    @Getter
    @Setter
    public static class Auth {

        @NotBlank(message = "이메일 사용자명은 필수입니다")
        @Email(message = "유효한 이메일 형식이어야 합니다")
        private String username;

        @NotBlank(message = "이메일 비밀번호는 필수입니다")
        private String password;

        private boolean enable = true;
    }
}
