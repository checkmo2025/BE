package checkmo.notification.web.dto;

import checkmo.global.dto.NotificationSharedDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class NotificationResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationListResponse {
        private List<NotificationSharedDTO.NotificationPreview> notifications;
        private boolean hasNext;        // 다음 페이지 존재 여부
        private Long nextCursor;        // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize;           // 현재 페이지 크기
    }
}
