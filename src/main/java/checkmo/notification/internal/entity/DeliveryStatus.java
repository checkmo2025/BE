package checkmo.notification.internal.entity;

public enum DeliveryStatus {
    PENDING,
    PROCESSING,
    TICKET_ACCEPTED,
    DELIVERED,
    RETRY_WAIT,
    PERMANENT_FAILED,
    CANCELLED
}
