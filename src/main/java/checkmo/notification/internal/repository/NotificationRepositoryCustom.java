package checkmo.notification.internal.repository;

import checkmo.notification.internal.entity.Notification;
import java.util.List;

public interface NotificationRepositoryCustom {

    List<Notification> findNotificationPreviews(String receiverId, int pageSize);

    List<Notification> findNotifications(String receiverId, Long cursorId, int pageSize);
}
