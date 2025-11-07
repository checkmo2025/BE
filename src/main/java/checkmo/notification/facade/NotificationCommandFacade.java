package checkmo.notification.facade;

/**
 * Notification Domain Command Facade
 *
 * Notification 도메인의 Command(생성, 수정, 삭제) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 */
public interface NotificationCommandFacade {

    /**
     * NotificationCommandService
     * 특정 알림을 읽음 상태로 처리합니다. (내부용)
     *
     * @param notificationId 읽음 처리할 알림 ID
     * @param memberId       요청한 회원의 ID (알림의 주인인지 확인용)
     */
    void markNotificationAsRead(Long notificationId, String memberId);
}