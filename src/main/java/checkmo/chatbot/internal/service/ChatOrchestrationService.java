package checkmo.chatbot.internal.service;

import checkmo.chatbot.internal.config.properties.ChatbotProperties;
import checkmo.chatbot.internal.entity.ChatMessage;
import checkmo.chatbot.internal.entity.ChatRole;
import checkmo.chatbot.internal.entity.ChatSession;
import checkmo.chatbot.internal.exception.ChatbotErrorStatus;
import checkmo.chatbot.internal.exception.ChatbotException;
import checkmo.chatbot.internal.prompt.ChatbotSystemPrompt;
import checkmo.chatbot.internal.repository.ChatMessageRepository;
import checkmo.chatbot.internal.repository.ChatSessionRepository;
import checkmo.chatbot.internal.service.dto.GeminiApiDTO.Content;
import checkmo.chatbot.internal.service.dto.GeminiApiDTO.Part;
import checkmo.common.monitoring.CheckmoMetrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 챗봇 한 턴을 처리하는 오케스트레이션. 세션 조회/생성 → PII 마스킹 → 프롬프트 조립 →
 * 규칙 기반 에스컬레이션 판단 → Gemini 호출 → 응답 후처리(미해결 태깅) → 저장 순서로 진행한다.
 *
 * Gemini 호출은 네트워크 I/O이므로, DB 트랜잭션을 길게 물고 있지 않도록 이 서비스 전체를
 * 하나의 @Transactional로 감싸지 않는다. 각 repository 호출이 각자 짧게 커밋된다.
 */
@Service
@RequiredArgsConstructor
public class ChatOrchestrationService {

    private static final int MAX_HISTORY_MESSAGES = 20;

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PiiMaskingService piiMaskingService;
    private final GeminiApiService geminiApiService;
    private final EscalationDecider escalationDecider;
    private final UnresolvedSessionTagger unresolvedSessionTagger;
    private final PromptLeakGuard promptLeakGuard;
    private final ChatbotProperties chatbotProperties;
    private final CheckmoMetrics checkmoMetrics;

    public ChatReply respond(String sessionToken, Long memberId, String rawUserMessage) {
        ChatSession chatSession = resolveSession(sessionToken, memberId);
        boolean wasUnresolved = chatSession.isUnresolved();
        chatSession.recordActivity();

        String maskedUserMessage = piiMaskingService.mask(rawUserMessage, memberId);
        boolean userNegativeReaction = unresolvedSessionTagger.isUserNegativeReaction(maskedUserMessage);
        if (userNegativeReaction) {
            chatSession.flagUnresolved();
        }

        List<Content> contents = appendUserTurn(buildHistory(chatSession.getId()), maskedUserMessage);
        chatMessageRepository.save(
                ChatMessage.userMessage(chatSession.getId(), maskedUserMessage, userNegativeReaction));

        boolean escalated = escalationDecider.shouldEscalate(maskedUserMessage, userNegativeReaction);
        String modelName = escalated
                ? chatbotProperties.getEscalationModel().getName()
                : chatbotProperties.getDefaultModel().getName();
        checkmoMetrics.incrementChatbotModelCall(modelName, escalated);

        // 봇 응답에는 마스킹을 적용하지 않는다. 모델은 이미 마스킹된 사용자 입력만 봤으므로 실제 PII를
        // 답변에 포함시킬 방법이 없고(구조적으로 안전), 반대로 "비밀번호는 6~12자..." 같은 정상 안내
        // 문장을 정규식이 PII로 오탐해 훼손하는 위험이 더 크다(실제 QA에서 확인됨).
        String reply = geminiApiService.generateReply(ChatbotSystemPrompt.SYSTEM_PROMPT, contents, modelName);

        if (promptLeakGuard.isLeaked(reply)) {
            checkmoMetrics.incrementChatbotPromptLeakDetected();
            reply = PromptLeakGuard.SAFE_FALLBACK_REPLY;
        }

        boolean botUncertain = unresolvedSessionTagger.isBotUncertain(reply);
        if (botUncertain) {
            chatSession.flagUnresolved();
        }

        chatMessageRepository.save(
                ChatMessage.assistantMessage(chatSession.getId(), reply, modelName, escalated, botUncertain));
        chatSessionRepository.save(chatSession);

        if (!wasUnresolved && chatSession.isUnresolved()) {
            checkmoMetrics.incrementChatbotUnresolvedSession();
        }

        return new ChatReply(
                chatSession.getSessionToken(),
                reply,
                escalated,
                modelName,
                chatSession.isUnresolved(),
                chatbotProperties.getHandoff().getSupportUrl(),
                chatbotProperties.getHandoff().getInquiryFormUrl()
        );
    }

    private ChatSession resolveSession(String sessionToken, Long memberId) {
        if (!StringUtils.hasText(sessionToken)) {
            return chatSessionRepository.save(ChatSession.start(memberId, UUID.randomUUID().toString()));
        }

        return chatSessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new ChatbotException(ChatbotErrorStatus.SESSION_NOT_FOUND));
    }

    private List<Content> buildHistory(Long chatSessionId) {
        List<ChatMessage> recentMessagesDesc = chatMessageRepository.findByChatSessionIdOrderByCreatedAtDesc(
                chatSessionId, PageRequest.of(0, MAX_HISTORY_MESSAGES));

        List<ChatMessage> recentMessagesAsc = new ArrayList<>(recentMessagesDesc);
        Collections.reverse(recentMessagesAsc);

        return recentMessagesAsc.stream().map(this::toContent).toList();
    }

    private List<Content> appendUserTurn(List<Content> history, String maskedUserMessage) {
        List<Content> contents = new ArrayList<>(history);
        contents.add(toContent(ChatRole.USER, maskedUserMessage));
        return contents;
    }

    private Content toContent(ChatMessage message) {
        return toContent(message.getRole(), message.getMaskedContent());
    }

    private Content toContent(ChatRole role, String text) {
        return Content.builder()
                .role(role == ChatRole.USER ? "user" : "model")
                .parts(List.of(Part.builder().text(text).build()))
                .build();
    }
}
