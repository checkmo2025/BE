package checkmo.notification.internal.converter;

import checkmo.notification.internal.entity.Notification;
import checkmo.notification.web.dto.NotificationResponseDTO;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationConverter {

    // =====================================================
    // Event → Notification 변환
    // =====================================================

    /**
     * Event -> Notification 변환
     */
    public static Notification fromEvent(
            Notification.NotificationType notificationType,
            String redirectPath,
            String targetName,
            String senderId,
            String receiverId
    ) {
        return Notification.builder()
                .notificationType(notificationType)
                .redirectPath(redirectPath)
                .targetName(targetName)
                .senderId(senderId)
                .receiverId(receiverId)
                .build();
    }

    /**
     * 리다이렉트 경로 반환
     */
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

    // =====================================================
    // Notification → NotificationExternalDTO 변환
    // =====================================================

    /**
     * NotificationPreviewDTO -> NotificationPreviewListDTO
     */
    public static NotificationResponseDTO.NotificationPreviewList convertToPreviewListDTO(
            List<Notification> notifications,
            Map<String, String> senderNicknameMap
    ) {

        List<NotificationResponseDTO.NotificationPreview> previewList = notifications.stream()
                .map(notification -> convertToPreviewDTO(
                        notification,
                        notification.getSenderId() != null ? senderNicknameMap.get(notification.getSenderId()) : null
                ))
                .toList();

        return NotificationResponseDTO.NotificationPreviewList.builder()
                .notifications(previewList)
                .build();
    }

    /**
     * Notification → NotificationExternalDTO 변환
     */
    public static NotificationResponseDTO.NotificationPreview convertToPreviewDTO(Notification notification,
                                                                                  String senderNickname) {
        return NotificationResponseDTO.NotificationPreview.builder()
                .notificationId(notification.getId())
                .notificationType(notification.getNotificationType())
                .senderNickname(senderNickname)
                .targetName(notification.getTargetName())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .redirectPath(notification.getRedirectPath())
                .build();
    }

    // =====================================================
    // Notification → NotificationResponseDTO 변환 (커서 기반 페이징용)
    // =====================================================

    /**
     * Notification -> NotificationListResponseDTO 변환
     */
    public static NotificationResponseDTO.NotificationList convertToNotificationListDTO(
            List<Notification> notifications,
            Map<String, String> senderNicknameMap,
            boolean hasNext,
            Long nextCursor,
            int pageSize
    ) {

        var notificationList = notifications.stream()
                .map(notification -> convertToPreviewDTO(
                        notification,
                        notification.getSenderId() != null ? senderNicknameMap.get(notification.getSenderId()) : null
                ))
                .toList();

        return NotificationResponseDTO.NotificationList.builder()
                .notifications(notificationList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .pageSize(pageSize)
                .build();
    }
}
