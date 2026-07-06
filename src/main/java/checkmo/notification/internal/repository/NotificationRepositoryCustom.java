package checkmo.notification.internal.repository;

import checkmo.notification.internal.entity.Notification;
import java.util.List;

public interface NotificationRepositoryCustom {

    List<Notification> findNotificationPreviews(Long receiverId, int pageSize);

    List<Notification> findNotifications(Long receiverId, Long cursorId, int pageSize);
}
