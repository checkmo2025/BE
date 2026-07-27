package checkmo.chatbot.internal.converter;

import checkmo.chatbot.internal.service.ChatReply;
import checkmo.chatbot.web.dto.ChatbotResponseDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatbotConverter {

    public static ChatbotResponseDTO.Reply toReplyResponse(ChatReply chatReply) {
        return ChatbotResponseDTO.Reply.builder()
                .sessionToken(chatReply.sessionToken())
                .replyText(chatReply.replyText())
                .escalated(chatReply.escalated())
                .modelUsed(chatReply.modelUsed())
                .handoffSuggested(chatReply.handoffSuggested())
                .supportUrl(chatReply.supportUrl())
                .inquiryFormUrl(chatReply.inquiryFormUrl())
                .build();
    }
}
