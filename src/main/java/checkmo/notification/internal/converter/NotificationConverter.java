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

    public static String getRedirectPath(Notification.NotificationType notificationType, Long bookStoryId) {
        if (Notification.NotificationType.LIKE == notificationType) {
            return "/bookstory/" + bookStoryId + "/detail"; // 프론트엔드 경로
        }
        return null; // 지금은 LIKE 타입일 경우 무조건 bookStoryId를 사용하지만, 다른 타입이 추가될 경우를 대비하여 null 반환
    }

    public static String getRedirectPath(Notification.NotificationType notificationType, String Nickname) {
        if (Notification.NotificationType.FOLLOW == notificationType) {
            return "/info/others/" + Nickname; // 프론트엔드 경로
        }
        return null; // 지금은 FOLLOW 타입일 경우 무조건 Nickname을 사용하지만, 다른 타입이 추가될 경우를 대비하여 null 반환
    }

    public static String getRedirectPathForClub(Notification.NotificationType notificationType, Long clubId) {
        if (Notification.NotificationType.JOIN_CLUB == notificationType) {
            return "/bookclub/" + clubId + "/home"; // 프론트엔드 경로
        }
        return null;
    }

    public static BasicInfoPreviewList convertToPreviewListDTO(
            List<Notification> notifications,
            Map<String, String> senderNicknameMap
    ) {

        List<BasicInfo> previewList = notifications.stream()
                .map(notification -> convertToPreviewDTO(
                        notification,
                        notification.getSenderId() != null ? senderNicknameMap.get(notification.getSenderId()) : null
                ))
                .toList();

        return BasicInfoPreviewList.builder()
                .notifications(previewList)
                .build();
    }

    public static BasicInfo convertToPreviewDTO(
            Notification notification,
            String senderNickname
    ) {
        return BasicInfo.builder()
                .notificationId(notification.getId())
                .notificationType(notification.getNotificationType())
                .senderNickname(senderNickname)
                .targetName(notification.getTargetName())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .redirectPath(notification.getRedirectPath())
                .build();
    }

    public static BasicInfoList convertToNotificationListDTO(
            List<Notification> notifications,
            Map<String, String> senderNicknameMap,
            CursorResult<Notification> cursorResult,
            int pageSize
    ) {

        var notificationList = notifications.stream()
                .map(notification -> convertToPreviewDTO(
                        notification,
                        notification.getSenderId() != null ? senderNicknameMap.get(notification.getSenderId()) : null
                ))
                .toList();

        return BasicInfoList.builder()
                .notifications(notificationList)
                .hasNext(cursorResult.hasNext())
                .nextCursor(cursorResult.nextCursor())
                .pageSize(pageSize)
                .build();
    }
}
