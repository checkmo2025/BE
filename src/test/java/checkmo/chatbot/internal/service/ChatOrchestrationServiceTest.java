package checkmo.chatbot.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.chatbot.internal.config.properties.ChatbotProperties;
import checkmo.chatbot.internal.entity.ChatSession;
import checkmo.chatbot.internal.exception.ChatbotException;
import checkmo.chatbot.internal.repository.ChatMessageRepository;
import checkmo.chatbot.internal.repository.ChatSessionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

class ChatOrchestrationServiceTest {

    private final ChatSessionRepository chatSessionRepository = mock(ChatSessionRepository.class);
    private final ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
    private final PiiMaskingService piiMaskingService = mock(PiiMaskingService.class);
    private final GeminiApiService geminiApiService = mock(GeminiApiService.class);
    private final EscalationDecider escalationDecider = mock(EscalationDecider.class);
    private final UnresolvedSessionTagger unresolvedSessionTagger = mock(UnresolvedSessionTagger.class);
    private final ChatbotProperties chatbotProperties = chatbotProperties();

    private ChatOrchestrationService chatOrchestrationService;

    @BeforeEach
    void setUp() {
        chatOrchestrationService = new ChatOrchestrationService(
                chatSessionRepository,
                chatMessageRepository,
                piiMaskingService,
                geminiApiService,
                escalationDecider,
                unresolvedSessionTagger,
                chatbotProperties
        );

        // save()는 넘어온 엔티티를 그대로 반환하되, id가 없으면 생성된 것처럼 채워준다.
        when(chatSessionRepository.save(any(ChatSession.class))).thenAnswer(invocation -> {
            ChatSession session = invocation.getArgument(0);
            if (session.getId() == null) {
                ReflectionTestUtils.setField(session, "id", 1L);
            }
            return session;
        });
        when(chatMessageRepository.findByChatSessionIdOrderByCreatedAtDesc(any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(piiMaskingService.mask(anyString(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(geminiApiService.generateReply(anyString(), anyList(), anyString())).thenReturn("책 이야기는 하단 탭에서 작성할 수 있어요.");
        when(escalationDecider.shouldEscalate(anyString(), anyBoolean())).thenReturn(false);
        when(unresolvedSessionTagger.isUserNegativeReaction(anyString())).thenReturn(false);
        when(unresolvedSessionTagger.isBotUncertain(anyString())).thenReturn(false);
    }

    @Test
    void createsNewSessionWhenTokenIsBlank() {
        ChatReply reply = chatOrchestrationService.respond(null, null, "책이야기 쓰려면?");

        assertThat(reply.sessionToken()).isNotBlank();
        assertThat(reply.replyText()).isEqualTo("책 이야기는 하단 탭에서 작성할 수 있어요.");
        assertThat(reply.modelUsed()).isEqualTo("gemini-3.1-flash-lite");
        assertThat(reply.escalated()).isFalse();
        assertThat(reply.handoffSuggested()).isFalse();
        assertThat(reply.supportUrl()).isEqualTo("http://localhost/support");
        assertThat(reply.inquiryFormUrl()).isEqualTo("http://localhost/inquiry");
    }

    @Test
    void reusesExistingSessionWhenTokenProvided() {
        ChatSession existing = ChatSession.start(5L, "existing-token");
        ReflectionTestUtils.setField(existing, "id", 10L);
        when(chatSessionRepository.findBySessionToken("existing-token")).thenReturn(Optional.of(existing));

        ChatReply reply = chatOrchestrationService.respond("existing-token", 5L, "모임 가입하려면?");

        assertThat(reply.sessionToken()).isEqualTo("existing-token");
        verify(chatSessionRepository).findBySessionToken("existing-token");
    }

    @Test
    void throwsWhenSessionTokenNotFound() {
        when(chatSessionRepository.findBySessionToken("missing-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatOrchestrationService.respond("missing-token", null, "질문"))
                .isInstanceOf(ChatbotException.class);
    }

    @Test
    void usesEscalationModelWhenDeciderSaysEscalate() {
        when(escalationDecider.shouldEscalate(anyString(), anyBoolean())).thenReturn(true);

        ChatReply reply = chatOrchestrationService.respond(null, null, "로그인이 계속 오류가 나요");

        assertThat(reply.escalated()).isTrue();
        assertThat(reply.modelUsed()).isEqualTo("gemini-3.6-flash");
    }

    @Test
    void suggestsHandoffWhenBotIsUncertain() {
        when(unresolvedSessionTagger.isBotUncertain(anyString())).thenReturn(true);

        ChatReply reply = chatOrchestrationService.respond(null, null, "아주 특이한 질문");

        assertThat(reply.handoffSuggested()).isTrue();
    }

    @Test
    void suggestsHandoffWhenUserShowsNegativeReaction() {
        when(unresolvedSessionTagger.isUserNegativeReaction(anyString())).thenReturn(true);

        ChatReply reply = chatOrchestrationService.respond(null, null, "말씀하신 대로 했는데 안 돼요");

        assertThat(reply.handoffSuggested()).isTrue();
    }

    private ChatbotProperties chatbotProperties() {
        ChatbotProperties properties = new ChatbotProperties();
        properties.setApiKey("test-key");
        properties.setBaseUrl("http://localhost");
        properties.setApiVersion("v1beta");
        properties.setTimeoutMs(1000);

        ChatbotProperties.Model defaultModel = new ChatbotProperties.Model();
        defaultModel.setName("gemini-3.1-flash-lite");
        properties.setDefaultModel(defaultModel);

        ChatbotProperties.Model escalationModel = new ChatbotProperties.Model();
        escalationModel.setName("gemini-3.6-flash");
        properties.setEscalationModel(escalationModel);

        ChatbotProperties.Handoff handoff = new ChatbotProperties.Handoff();
        handoff.setSupportUrl("http://localhost/support");
        handoff.setInquiryFormUrl("http://localhost/inquiry");
        properties.setHandoff(handoff);

        return properties;
    }
}
