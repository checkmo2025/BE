package checkmo.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationListResponseDTO {
        private List<NotificationInfoResponseDTO> notifications;
        private boolean hasNext;        // 다음 페이지 존재 여부
        private Long nextCursor;        // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize;           // 현재 페이지 크기
    }



    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationInfoResponseDTO {
        private Long NotificationId;
        private String senderNickname;
        private String receiverNickname;
        private boolean isRead;
        private LocalDateTime createdAt;
        private String redirectPath;
    }
}
