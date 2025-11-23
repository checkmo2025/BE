package checkmo.notification.internal.service.query;

import checkmo.notification.internal.entity.Notification;
import checkmo.notification.internal.repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
     * @param memberId 회원 ID
     * @param cursorId 페이징을 위한 커서 ID (처음에는 null)
     * @param pageSize 페이지 크기
     * @return 조회된 알림 엔티티 목록
     */
    public List<Notification> retrieveNotifications(String memberId, Long cursorId, int pageSize) {
        // 커서 기반으로 알림 목록 조회 (페이지 크기 + 1개 조회)
        if (cursorId == null) {
            // 첫 페이지: 가장 최근 알림부터 조회
            return notificationRepository.findByReceiverIdOrderByIdDesc(memberId, PageRequest.of(0, pageSize));
        } else {
            // 다음 페이지: 커서보다 작은 ID의 알림 조회
            return notificationRepository.findByReceiverIdAndIdLessThanOrderByIdDesc(memberId, cursorId,
                    PageRequest.of(0, pageSize));
        }
    }

    /**
     * 읽지 않은 알림 엔티티 목록 조회
     *
     * @param receiverId 알림을 받을 회원 ID
     * @param size       조회할 알림 개수
     * @return 읽지 않은 알림 엔티티 목록
     */
    public List<Notification> retrieveUnreadNotifications(String receiverId, int size) {
        return notificationRepository
                .findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(receiverId, PageRequest.of(0, size));
    }
}
