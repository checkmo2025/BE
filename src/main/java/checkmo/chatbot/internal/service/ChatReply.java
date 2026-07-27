package checkmo.chatbot.internal.service;

/**
 * 챗봇 오케스트레이션 결과. Stage 5 컨트롤러가 이 값을 그대로 응답 DTO로 변환한다.
 */
public record ChatReply(
        String sessionToken,
        String replyText,
        boolean escalated,
        String modelUsed,
        boolean handoffSuggested,
        String supportUrl,
        String inquiryFormUrl
) {
}
