package checkmo.notification.internal.repository;

import checkmo.notification.internal.entity.Notification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

    Optional<Notification> findByIdAndReceiverId(Long notificationId, Long receiverId);
}
