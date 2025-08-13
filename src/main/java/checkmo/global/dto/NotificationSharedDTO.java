package checkmo.global.dto;

import checkmo.domain.notification.entity.Notification;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    public static class NotificationPreviewList {
        private List<NotificationPreview> notifications;
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
    public static class NotificationPreview {
        private Long notificationId;
        private Notification.NotificationType notificationType; // 알림 타입
        private String senderNickname;
        private String targetName; // 대상 이름 (클럽명, 사용자명 등)
        private boolean read;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdAt;
        private String redirectPath; // 알림 클릭 시 이동할 URL
    }
}