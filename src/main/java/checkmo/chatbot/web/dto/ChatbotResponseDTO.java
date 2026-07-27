package checkmo.chatbot.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatbotResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Reply {

        @Schema(description = "이 대화의 세션 토큰. 다음 요청에 그대로 실어 보내면 같은 대화로 이어집니다.")
        private String sessionToken;

        @Schema(description = "챗봇 응답 텍스트")
        private String replyText;

        @Schema(description = "복잡한 질문으로 판단되어 고급 모델로 에스컬레이션되었는지 여부")
        private boolean escalated;

        @Schema(description = "이번 응답을 생성한 모델명")
        private String modelUsed;

        @Schema(description = "챗봇으로 해결이 안 되는 것으로 보여 상담사 연결을 제안해야 하는지 여부")
        private boolean handoffSuggested;

        @Schema(description = "상담사 연결(고객센터) 외부 링크")
        private String supportUrl;

        @Schema(description = "상담사 연결(문의 폼) 외부 링크")
        private String inquiryFormUrl;
    }
}
