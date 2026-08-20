package checkmo.chatbot.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ChatSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long memberId; // 비로그인 사용자는 null

    @Column(nullable = false, unique = true, length = 64)
    private String sessionToken;

    @Column(nullable = false)
    private boolean unresolved;

    @Column(nullable = false)
    private LocalDateTime lastActivityAt;

    private ChatSession(Long memberId, String sessionToken) {
        this.memberId = memberId;
        this.sessionToken = sessionToken;
        this.unresolved = false;
        this.lastActivityAt = LocalDateTime.now();
    }

    public static ChatSession start(Long memberId, String sessionToken) {
        return new ChatSession(memberId, sessionToken);
    }

    public void recordActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    public void flagUnresolved() {
        this.unresolved = true;
    }
}
