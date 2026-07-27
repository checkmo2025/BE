package checkmo.chatbot.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chatSessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRole role;

    // 마스킹된 텍스트만 저장한다. 원문(PII 포함 가능)은 어떤 컬럼에도 저장하지 않는다.
    // @Lob만 쓰면 기본 length(255) 때문에 Hibernate가 TINYTEXT를 기대해 TEXT 컬럼과 스키마 검증이 어긋난다.
    @Column(nullable = false, columnDefinition = "TEXT")
    private String maskedContent;

    @Column(length = 100)
    private String modelUsed; // ASSISTANT 메시지에만 사용

    @Column(nullable = false)
    private boolean escalated;

    @Column(nullable = false)
    private boolean botUncertain;

    @Column(nullable = false)
    private boolean userNegativeReaction;

    private ChatMessage(
            Long chatSessionId,
            ChatRole role,
            String maskedContent,
            String modelUsed,
            boolean escalated,
            boolean botUncertain,
            boolean userNegativeReaction
    ) {
        this.chatSessionId = chatSessionId;
        this.role = role;
        this.maskedContent = maskedContent;
        this.modelUsed = modelUsed;
        this.escalated = escalated;
        this.botUncertain = botUncertain;
        this.userNegativeReaction = userNegativeReaction;
    }

    public static ChatMessage userMessage(Long chatSessionId, String maskedContent, boolean userNegativeReaction) {
        return new ChatMessage(chatSessionId, ChatRole.USER, maskedContent, null, false, false, userNegativeReaction);
    }

    public static ChatMessage assistantMessage(
            Long chatSessionId,
            String maskedContent,
            String modelUsed,
            boolean escalated,
            boolean botUncertain
    ) {
        return new ChatMessage(chatSessionId, ChatRole.ASSISTANT, maskedContent, modelUsed, escalated, botUncertain, false);
    }
}
