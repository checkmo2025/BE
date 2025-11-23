package checkmo.notification.internal.service.command;

import checkmo.bookStory.BookStoryEvent;
import checkmo.clubManagement.ClubManagementEvent.JoinClubEvent;
import checkmo.member.MemberAPI;
import checkmo.member.MemberEvent;
import checkmo.notification.internal.converter.NotificationConverter;
import checkmo.notification.internal.entity.Notification;
import checkmo.notification.internal.entity.Notification.NotificationType;
import checkmo.notification.internal.exception.NotificationErrorStatus;
import checkmo.notification.internal.exception.NotificationException;
import checkmo.notification.internal.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandService {

    private final MemberAPI memberAPI;

    private final NotificationRepository notificationRepository;

    /**
     * 좋아요 알림 생성
     *
     * @param event 좋아요 알림 정보 DTO
     */
    @CacheEvict(value = "notifications", key = "#event.receiverId()")
    public void createNotification(BookStoryEvent.BookStoryLiked event) {
        NotificationType type = NotificationType.LIKE;
        Long sourceId = event.eventId();
        if (notificationRepository.existsByNotificationTypeAndSourceId(type, sourceId)) {
            return;
        }

        String redirectPath = NotificationConverter.getRedirectPath(type, event.bookStoryId());

        // Notification 객체를 생성하고 저장 (targetName = null)
        Notification notification = Notification.builder()
                .notificationType(type)
                .sourceId(sourceId)
                .redirectPath(redirectPath)
                .targetName(null)
                .senderId(event.senderId())
                .receiverId(event.receiverId())
                .build();
        try {
            notificationRepository.save(notification);
        } catch (DataIntegrityViolationException e) {
            // 다른 인스턴스가 동일 알람을 저장한 경우 -> 무시
        }
    }

    /**
     * 팔로우(구독) 알림 생성
     *
     * @param event 팔로우 알림 정보 DTO
     */
    @CacheEvict(value = "notifications", key = "#event.followingId()")
    public void createNotification(MemberEvent.Follow event) {
        Notification.NotificationType type = Notification.NotificationType.FOLLOW;
        Long sourceId = event.eventId();
        if (notificationRepository.existsByNotificationTypeAndSourceId(type, sourceId)) {
            return;
        }

        // 팔로우 누른 사람의 닉네임을 가져옴
        String FollowerNickname = memberAPI.fetchNickname(event.followerId());

        String redirectPath = NotificationConverter.getRedirectPath(type, FollowerNickname);

        // Notification 객체를 생성하고 저장 (targetName = followerNickname)
        Notification notification = Notification.builder()
                .notificationType(type)
                .sourceId(sourceId)
                .redirectPath(redirectPath)
                .targetName(FollowerNickname)
                .senderId(event.followerId())
                .receiverId(event.followingId())
                .build();
        try {
            notificationRepository.save(notification);
        } catch (DataIntegrityViolationException e) {
            // 다른 인스턴스가 동일 알람을 저장한 경우 -> 무시
        }
    }

    /**
     * 독서 클럽 가입 승인 알림 생성
     *
     * @param event 독서 클럽 가입 승인 알림 정보 DTO
     */
    @CacheEvict(value = "notifications", key = "#event.memberId()")
    public void createNotification(JoinClubEvent event) {
        Notification.NotificationType type = Notification.NotificationType.JOIN_CLUB;
        Long sourceId = event.eventId();
        if (notificationRepository.existsByNotificationTypeAndSourceId(type, sourceId)) {
            return;
        }

        String redirectPath
                = NotificationConverter.getRedirectPathForClub(Notification.NotificationType.JOIN_CLUB, event.clubId());

        // Notification 객체를 생성하고 저장 (sender 없이, targetName 포함)
        Notification notification = Notification.builder()
                .notificationType(type)
                .sourceId(sourceId)
                .redirectPath(redirectPath)
                .targetName(event.clubName())
                .senderId(null)
                .receiverId(event.memberId())
                .build();
        try {
            notificationRepository.save(notification);
        } catch (DataIntegrityViolationException e) {
            // 다른 인스턴스가 동일 알람을 저장한 경우 -> 무시
        }
    }

    /**
     * 알림 읽음 처리
     *
     * @param notificationId 읽음 처리할 알림 ID
     * @param memberId       읽음 처리할 회원 ID (receiverId)
     */
    @CacheEvict(value = "notifications", key = "#memberId")
    public void markNotificationAsRead(Long notificationId, String memberId) {
        Notification notification = notificationRepository.findByIdAndReceiverId(notificationId, memberId)
                .orElseThrow(() -> new NotificationException(NotificationErrorStatus.NOTIFICATION_NOT_FOUND));

        // 알림이 이미 읽음 상태인지 확인
        if (notification.isRead()) {
            throw new NotificationException(NotificationErrorStatus.NOTIFICATION_ALREADY_READ);
        }

        // 알림을 읽음 상태로 변경
        notification.markAsRead();
    }
}
