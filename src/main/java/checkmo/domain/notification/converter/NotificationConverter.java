package checkmo.domain.notification.converter;

import checkmo.domain.member.entity.Member;
import checkmo.domain.notification.entity.Notification;
import checkmo.domain.notification.web.dto.NotificationResponseDTO;
import checkmo.global.dto.NotificationSharedDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

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
            Member proxySender,
            Member proxyReceiver
    ) {
        return Notification.builder()
                .notificationType(notificationType)
                .redirectPath(redirectPath)
                .sender(proxySender)
                .receiver(proxyReceiver)
                .build();
    }

    /**
     * 리다이렉트 경로 반환
     */
    public static String getRedirectPath(Notification.NotificationType notificationType, Long bookStoryId) {
        if (Notification.NotificationType.LIKE == notificationType) {
            return "/api/book-stories/" + bookStoryId;
        }
        return null; // 지금은 LIKE 타입일 경우 무조건 bookStoryId를 사용하지만, 다른 타입이 추가될 경우를 대비하여 null 반환
    }

    public static String getRedirectPath(Notification.NotificationType notificationType, String Nickname) {
        if (Notification.NotificationType.FOLLOW == notificationType) {
            return "/api/members/" + Nickname;
        }
        return null; // 지금은 FOLLOW 타입일 경우 무조건 Nickname을 사용하지만, 다른 타입이 추가될 경우를 대비하여 null 반환
    }

    // =====================================================
    // Notification → NotificationSharedDTO 변환
    // =====================================================

    /**
     * NotificationPreviewDTO -> NotificationPreviewListDTO
     */
    public static NotificationSharedDTO.NotificationPreviewList convertToPreviewListDTO(
            List<Notification> notifications,
            Map<String, String> senderNicknameMap
    ) {

        List<NotificationSharedDTO.NotificationPreview> previewList = notifications.stream()
                .map(notification -> convertToPreviewDTO(
                        notification,
                        senderNicknameMap.get(notification.getSenderId())
                ))
                .toList();

        return NotificationSharedDTO.NotificationPreviewList.builder()
                .notifications(previewList)
                .build();
    }

    /**
     * Notification → NotificationSharedDTO 변환
     */
    public static NotificationSharedDTO.NotificationPreview convertToPreviewDTO(Notification notification, String senderNickname) {
        return NotificationSharedDTO.NotificationPreview.builder()
                .notificationId(notification.getId())
                .notificationType(notification.getNotificationType())
                .senderNickname(senderNickname)
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
    public static NotificationResponseDTO.NotificationListResponse convertToNotificationListDTO(
            List<Notification> notifications,
            Map<String, String> senderNicknameMap,
            boolean hasNext,
            Long nextCursor,
            int pageSize
    ) {

        var notificationList = notifications.stream()
                .map(notification -> convertToPreviewDTO(
                        notification, 
                        senderNicknameMap.get(notification.getSenderId())
                ))
                .toList();

        return NotificationResponseDTO.NotificationListResponse.builder()
                .notifications(notificationList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .pageSize(pageSize)
                .build();
    }
}
