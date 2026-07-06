package checkmo.notification.internal.service.query;

import checkmo.notification.internal.entity.Notification;
import checkmo.notification.internal.repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    /**
     * 커서 기반으로 알림 엔티티 목록을 조회
     *
     * @param receiverId 알림을 조회하는 회원 ID
     * @param cursorId   페이징을 위한 커서 ID (처음에는 null)
     * @param size       페이지 크기
     * @return 조회된 알림 엔티티 목록
     */
    public List<Notification> retrieveNotifications(Long receiverId, Long cursorId, int size) {
        return notificationRepository.findNotifications(receiverId, cursorId, size);
    }

    /**
     * 읽지 않은 알림 엔티티 목록 조회
     *
     * @param receiverId 알림을 조회하는 회원 ID
     * @param size       조회할 알림 개수
     * @return 읽지 않은 알림 엔티티 목록
     */
    public List<Notification> retrieveUnreadNotifications(Long receiverId, int size) {
        return notificationRepository.findNotificationPreviews(receiverId, size);
    }
}
