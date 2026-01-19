package checkmo.notification.internal.converter;

import checkmo.common.template.CursorResult;
import checkmo.notification.internal.entity.Notification;
import checkmo.notification.web.dto.NotificationResponseDTO.BasicInfo;
import checkmo.notification.web.dto.NotificationResponseDTO.BasicInfoList;
import checkmo.notification.web.dto.NotificationResponseDTO.BasicInfoPreviewList;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationConverter {

    public static BasicInfoPreviewList convertToPreviewListDTO(
            List<Notification> notifications,
            Map<String, String> senderNicknameMap,
            Map<Long, String> clubNameMap
    ) {
        List<BasicInfo> previewList = notifications.stream()
                .map(notification -> convertToBasicInfo(notification, senderNicknameMap, clubNameMap))
                .toList();

        return BasicInfoPreviewList.builder()
                .notifications(previewList)
                .build();
    }

    public static BasicInfoList convertToNotificationListDTO(
            List<Notification> notifications,
            Map<String, String> senderNicknameMap,
            Map<Long, String> clubNameMap,
            CursorResult<Notification> cursorResult,
            int pageSize
    ) {
        var notificationList = notifications.stream()
                .map(notification -> convertToBasicInfo(notification, senderNicknameMap, clubNameMap))
                .toList();

        return BasicInfoList.builder()
                .notifications(notificationList)
                .hasNext(cursorResult.hasNext())
                .nextCursor(cursorResult.nextCursor())
                .pageSize(pageSize)
                .build();
    }

    private static BasicInfo convertToBasicInfo(
            Notification notification,
            Map<String, String> senderNicknameMap,
            Map<Long, String> clubNameMap
    ) {
        // 클럽 알림: clubName 사용,
        // 사용자 알림: senderNickname 사용
        String displayName = isClubNotification(notification.getNotificationType())
                ? clubNameMap.get(notification.getDomainId())
                : senderNicknameMap.get(notification.getSenderId());

        // sourceId는 클럽 미팅/공지 알림에서만 필요
        Long sourceId = needsSourceId(notification.getNotificationType())
                ? notification.getSourceId()
                : null;

        return BasicInfo.builder()
                .notificationId(notification.getId())
                .notificationType(notification.getNotificationType())
                .domainId(notification.getDomainId())
                .sourceId(sourceId)
                .displayName(displayName)
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private static boolean isClubNotification(Notification.NotificationType type) {
        return type == Notification.NotificationType.JOIN_CLUB
                || type == Notification.NotificationType.CLUB_MEETING_CREATED
                || type == Notification.NotificationType.CLUB_NOTICE_CREATED;
    }

    private static boolean needsSourceId(Notification.NotificationType type) {
        return type == Notification.NotificationType.CLUB_MEETING_CREATED
                || type == Notification.NotificationType.CLUB_NOTICE_CREATED;
    }
}
