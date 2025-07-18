package checkmo.domain.notification.facade;

import checkmo.domain.notification.web.dto.NotificationResponseDTO;
import checkmo.global.dto.NotificationSharedDTO;

/**
 * Notification Domain Query Facade
 *
 * Notification 도메인의 Query(조회) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 */
public interface NotificationQueryFacade {

    /**
     * 특정 회원의 읽지 않은 알림 개수를 조회합니다. (외부용)
     * 네비게이션 바 등 공통 UI에서 알림 뱃지를 표시할 때 사용됩니다.
     *
     * @param memberId 회원 ID
     * @return 읽지 않은 알림 개수
     */
    Long getUnreadNotificationCount(String memberId);

    /**
     * 특정 회원의 알림 미리보기 목록을 size 개수만큼 조회합니다. (외부용)
     * 홈 화면 등 다른 서비스에서 최신 알림을 보여줄 때 사용됩니다.
     *
     * @param memberId 회원 ID
     * @param size     조회할 개수
     * @return 알림 미리보기 정보가 담긴 **공유 DTO**
     */
    NotificationSharedDTO.NotificationPreviewListDTO getNotificationPreviewsForShare(String memberId, int size);

    /**
     * 특정 회원의 알림 목록을 커서 기반으로 조회합니다. (내부용)
     *
     * @param memberId 회원 ID
     * @param cursorId 페이징을 위한 커서 ID (처음에는 null)
     * @return 알림 목록 정보 DTO
     */
    NotificationResponseDTO.NotificationListResponseDTO getNotifications(Long memberId, Long cursorId);
}