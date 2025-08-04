package checkmo.domain.notification.entity;

import checkmo.domain.member.entity.Member;
import checkmo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Notification extends BaseEntity {

    public enum NotificationType {
        LIKE, FOLLOW
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @Builder.Default
    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private String redirectPath; // 알림 클릭 시 이동할 페이지의 경로

    @Column(name = "receiver_id", insertable = false, updatable = false)
    private String receiverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Member receiver;

    @Column(name = "sender_id", insertable = false, updatable = false)
    private String senderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Member sender;

    public void markAsRead() {
        this.isRead = true;
    }
}
