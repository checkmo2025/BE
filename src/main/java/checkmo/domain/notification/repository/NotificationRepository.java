package checkmo.domain.notification.repository;

import checkmo.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 특정 사용자의 읽지 않은 알림을 최신순으로 조회
    List<Notification> findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(String receiverId, Pageable pageable);

    // 특정 사용자의 모든 알림을 최신순으로 조회 (첫 페이지용)
    List<Notification> findByReceiverIdOrderByIdDesc(String receiverId, Pageable pageable);
    
    // 특정 사용자의 모든 알림을 커서보다 작은 ID로 조회 (커서 기반 페이징용)
    List<Notification> findByReceiverIdAndIdLessThanOrderByIdDesc(String receiverId, Long cursorId, Pageable pageable);

    // 특정 알림을 읽음 처리할 때 해당 알림이 사용자의 것인지 확인
    Optional<Notification> findByIdAndReceiverId(Long notificationId, String receiverId);
}
