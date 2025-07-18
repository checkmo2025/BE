package checkmo.domain.notification.service.query;

import checkmo.domain.notification.web.dto.NotificationResponseDTO;

/**
 * 알림 조회 서비스
 *
 * 알림은 회원에게 전달되는 것을 말함
 * ex) 모임 공지사항, 알림 목록 조회, 읽지 않은 알림 개수 확인 등등
 */
public interface NotificationQueryService {
    /**
     * 알림 목록 조회
     *
     * @param memberId receiver ID
     * @return 전체 알림 목록
     */
    NotificationResponseDTO.NotificationListResponseDTO getNotifications(Long memberId, Long cursorId);

    /**
     * size개의 알림 목록 조회
     *
     * @param memberId receiver ID
     * @param size 몇개를 조회할지
     * @return 알림 목록 size개
     */
    NotificationResponseDTO.NotificationListResponseDTO getNotifications(Long memberId, int size);
}
