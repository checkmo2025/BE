package checkmo.notification.web.dto;

import checkmo.notification.internal.entity.Notification;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
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
    public static class BasicInfoList {
        private List<BasicInfo> notifications;
        private boolean hasNext;
        private Long nextCursor;
        private int pageSize;
    }

    /**
     * 알림 미리보기 목록 응답 DTO 홈 화면 같은 곳에서 알림을 몇개만 보여줄 때 사용
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BasicInfoPreviewList {
        private List<BasicInfo> notifications;
    }

    /**
     * 알림 미리보기 정보 DTO 알림의 기본 정보(발신자, 수신자, 읽음 상태, 생성일, 리다이렉트 경로)를 포함 알림 목록에서 각 알림을 간략히 표시할 때 사용 푸시 알림이나 실시간 알림에서도 활용
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BasicInfo {
        private Long notificationId;
        private Notification.NotificationType notificationType;
        private String senderNickname;
        private String targetName; // 대상 이름 (클럽명, 사용자명 등)
        private boolean read;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdAt;
        private String redirectPath;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SettingInfo {
        private boolean bookStoryLiked;
        private boolean bookStoryComment;
        private boolean clubNoticeCreated;
        private boolean clubMeetingCreated;
        private boolean newFollower;
    }
}
