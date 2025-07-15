package checkmo.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationSharedDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationPreviewListDTO {
        private List<NotificationPreviewDTO> notifications;
        private boolean hasNext;
        private Long nextCursor;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationPreviewDTO {
        private Long notificationId;
        private String senderNickname;
        private String receiverNickname;
        private boolean isRead;
        private LocalDateTime createdAt;
        private String redirectPath; // 알림 클릭 시 이동할 URL
    }
}