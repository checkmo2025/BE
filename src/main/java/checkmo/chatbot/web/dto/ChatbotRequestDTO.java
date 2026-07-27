package checkmo.chatbot.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatbotRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class Message {

        @Schema(
                description = "이전 대화를 이어가려면 이전 응답에서 받은 sessionToken을 그대로 보냅니다. "
                        + "처음 대화를 시작하는 경우 이 필드 자체를 요청 바디에서 빼거나 JSON null로 보내세요. "
                        + "문자열 \"null\"을 값으로 보내면 존재하지 않는 세션을 조회한 것으로 처리되어 404가 발생합니다.",
                nullable = true,
                example = ""
        )
        private String sessionToken;

        @NotBlank(message = "메시지는 필수입니다.")
        @Schema(description = "사용자가 입력한 질문", example = "책이야기 쓰려면?")
        private String message;
    }
}
