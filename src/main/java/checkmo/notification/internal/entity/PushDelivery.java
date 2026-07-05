package checkmo.notification.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_push_delivery_notification_device",
                        columnNames = {"notification_id", "push_device_id"}
                )
        },
        indexes = {
                @Index(name = "idx_push_delivery_expo_ticket_id", columnList = "expo_ticket_id"),
                @Index(name = "idx_push_delivery_next_attempt_at", columnList = "next_attempt_at")
        }
)
public class PushDelivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "push_device_id", nullable = false)
    private PushDevice pushDevice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeliveryStatus status;

    @Column(name = "expo_ticket_id", length = 100)
    private String expoTicketId;

    @Builder.Default
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "last_error_code", length = 100)
    private String lastErrorCode;

    @Column(name = "last_error_message", length = 500)
    private String lastErrorMessage;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "receipt_checked_at")
    private LocalDateTime receiptCheckedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    public void markProcessing() {
        this.status = DeliveryStatus.PROCESSING;
        this.processingStartedAt = LocalDateTime.now();
    }

    public void markTicketAccepted(String ticketId) {
        this.status = DeliveryStatus.TICKET_ACCEPTED;
        this.expoTicketId = ticketId;
        this.sentAt = LocalDateTime.now();
    }

    public void markDelivered() {
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    public void markRetryWait(LocalDateTime nextAt, String errorCode, String errorMessage) {
        this.status = DeliveryStatus.RETRY_WAIT;
        this.nextAttemptAt = nextAt;
        this.lastErrorCode = errorCode;
        this.lastErrorMessage = errorMessage;
        this.attemptCount++;
    }

    public void markPermanentFailed(String errorCode, String errorMessage) {
        this.status = DeliveryStatus.PERMANENT_FAILED;
        this.lastErrorCode = errorCode;
        this.lastErrorMessage = errorMessage;
    }

    public void markCancelled() {
        this.status = DeliveryStatus.CANCELLED;
    }

    public void updateReceiptCheckedAt(LocalDateTime checkedAt) {
        this.receiptCheckedAt = checkedAt;
    }
}
