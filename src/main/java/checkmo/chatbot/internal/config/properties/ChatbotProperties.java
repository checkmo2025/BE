package checkmo.chatbot.internal.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "chatbot.gemini")
public class ChatbotProperties {

    @NotBlank(message = "Gemini API 키는 필수입니다")
    private String apiKey;

    @NotBlank(message = "Gemini API 기본 URL은 필수입니다")
    private String baseUrl = "https://generativelanguage.googleapis.com";

    @NotBlank(message = "Gemini API 버전은 필수입니다")
    private String apiVersion = "v1beta";

    @Positive(message = "타임아웃은 양수여야 합니다")
    private int timeoutMs = 15000;

    @Valid
    private Model defaultModel = new Model();

    @Valid
    private Model escalationModel = new Model();

    @Valid
    private Handoff handoff = new Handoff();

    @Getter
    @Setter
    public static class Model {
        @NotBlank(message = "모델명은 필수입니다")
        private String name;
    }

    @Getter
    @Setter
    public static class Handoff {
        @NotBlank(message = "고객센터 URL은 필수입니다")
        private String supportUrl;

        @NotBlank(message = "문의 폼 URL은 필수입니다")
        private String inquiryFormUrl;
    }
}
