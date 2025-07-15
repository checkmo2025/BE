package checkmo.domain.notification;

import checkmo.domain.notification.dto.NotificationRequestDTO;

/**
 * Notification Domain Command Facade
 *
 * Notification 도메인의 Command(생성, 수정, 삭제) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 */
public interface NotificationCommandFacade {

    /**
     * NotificationCommandService
     * '좋아요' 이벤트에 대한 알림을 생성합니다. (외부용)
     * BookStory 도메인 등에서 호출됩니다.
     *
     * @param request 좋아요 알림 생성에 필요한 정보 DTO
     */
    void createLikeNotification(NotificationRequestDTO.LikeRequestDTO request);

    /**
     * NotificationCommandService
     * '팔로우' 이벤트에 대한 알림을 생성합니다. (외부용)
     * Member 도메인 등에서 호출됩니다.
     *
     * @param request 팔로우 알림 생성에 필요한 정보 DTO
     */
    void createFollowNotification(NotificationRequestDTO.FollowRequestDTO request);

    /**
     * NotificationCommandService
     * 특정 알림을 읽음 상태로 처리합니다. (내부용)
     *
     * @param notificationId 읽음 처리할 알림 ID
     * @param memberId       요청한 회원의 ID (알림의 주인인지 확인용)
     */
    void markNotificationAsRead(Long notificationId, String memberId);
}