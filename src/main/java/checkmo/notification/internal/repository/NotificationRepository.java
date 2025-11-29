package checkmo.notification.internal.repository;

import checkmo.notification.internal.entity.Notification;
import checkmo.notification.internal.entity.Notification.NotificationType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

    Optional<Notification> findByIdAndReceiverId(Long notificationId, String receiverId);

    boolean existsByNotificationTypeAndSourceId(NotificationType notificationType, Long sourceId);
}