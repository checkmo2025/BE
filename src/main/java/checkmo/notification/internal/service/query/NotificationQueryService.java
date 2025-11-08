package checkmo.notification.internal.service.query;

import checkmo.notification.entity.Notification;

import java.util.List;

/**
 * 알림 조회 서비스
 *
 * 알림은 회원에게 전달되는 것을 말함
 * ex) 모임 공지사항, 알림 목록 조회, 읽지 않은 알림 개수 확인 등등
 */
public interface NotificationQueryService {
    /**
     * 커서 기반으로 알림 엔티티 목록을 조회 (페이지네이션을 위해 +1개 더 조회)
     *
     * @param memberId 회원 ID
     * @param cursorId 페이징을 위한 커서 ID (처음에는 null)
     * @param pageSize 페이지 크기
     * @return 조회된 알림 엔티티 목록
     */
    List<Notification> findNotifications(String memberId, Long cursorId, int pageSize);

    /**
     * 읽지 않은 알림 엔티티 목록 조회
     *
     * @param receiverId 알림을 받을 회원 ID
     * @param size 조회할 알림 개수
     * @return 읽지 않은 알림 엔티티 목록
     */
    List<Notification> findUnreadNotifications(String receiverId, int size);
}
