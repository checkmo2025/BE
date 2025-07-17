package checkmo.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 도메인과 관련된 공유 DTO 클래스
 * 다른 도메인에서 알림 정보를 참조할 때 사용
 */
public class NotificationSharedDTO {

    /**
     * 알림 미리보기 목록 응답 DTO
     * 홈 화면 같은 곳에서 알림을 몇개만 보여줄 때 사용
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationPreviewListDTO {
        private List<NotificationPreviewDTO> notifications;
    }

    /**
     * 알림 미리보기 정보 DTO
     * 알림의 기본 정보(발신자, 수신자, 읽음 상태, 생성일, 리다이렉트 경로)를 포함
     * 알림 목록에서 각 알림을 간략히 표시할 때 사용
     * 푸시 알림이나 실시간 알림에서도 활용
     */
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