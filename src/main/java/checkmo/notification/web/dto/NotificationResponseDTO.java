package checkmo.notification.web.dto;

import checkmo.notification.NotificationExternalDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class NotificationResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationListResponse {
        private List<NotificationExternalDTO.NotificationPreview> notifications;
        private boolean hasNext;        // 다음 페이지 존재 여부
        private Long nextCursor;        // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize;           // 현재 페이지 크기
    }
}
