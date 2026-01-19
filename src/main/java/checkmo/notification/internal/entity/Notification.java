package checkmo.notification.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"notification_type", "source_id", "receiver_id"})
})
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId; //FOLLOW.id, BOOK_STORY_LIKED.id, CLUB_MEMBER.id 등 알림의 출처가 되는 엔티티 ID

    @Builder.Default
    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private String redirectPath;

    @Column
    private String targetName; // 대상 엔티티의 이름 (클럽명, 사용자명 등)

    @Column(name = "receiver_id", nullable = false)
    private String receiverId;

    @Column(name = "sender_id", nullable = false)
    private String senderId;

    public void markAsRead() {
        this.isRead = true;
    }

    public enum NotificationType {
        LIKE, COMMENT, FOLLOW, JOIN_CLUB, CLUB_MEETING_CREATED, CLUB_NOTICE_CREATED
    }
}
