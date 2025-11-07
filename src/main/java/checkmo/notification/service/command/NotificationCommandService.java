package checkmo.notification.service.command;

import checkmo.event.FollowEvent;
import checkmo.event.JoinClubEvent;
import checkmo.event.LikeEvent;

/**
 * 알림 생성, 수정, 삭제 서비스
 *
 * 알림 자체의 생성, 수정, 삭제 담당
 * 알림은 회원에게 전달되는 것을 말함
 * ex) 모임 공지사항, 팔로우 알림 등등
 */
public interface NotificationCommandService {

    /**
     * 좋아요 알림 생성
     *
     * @param event 좋아요 알림 정보 DTO
     */
    void createNotification(LikeEvent event);

    /**
     * 팔로우(구독) 알림 생성
     *
     * @param event 팔로우 알림 정보 DTO
     */
     void createNotification(FollowEvent event);

    /**
     * 독서 클럽 가입 승인 알림 생성
     *
     * @param event 독서 클럽 가입 승인 알림 정보 DTO
     */
    void createNotification(JoinClubEvent event);

    /**
     * 알림 읽음 처리
     * @param notificationId 읽음 처리할 알림 ID
     * @param memberId 읽음 처리할 회원 ID (receiverId)
     */
    void markNotificationAsRead(Long notificationId, String memberId);
}
