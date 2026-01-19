package checkmo.notification.internal.service.command;

import checkmo.bookStory.BookStoryEvent;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementEvent.JoinClubEvent;
import checkmo.clubMeeting.ClubMeetingEvent.ClubMeetingCreated;
import checkmo.clubNotice.ClubNoticeEvent.ClubNoticeCreated;
import checkmo.member.MemberEvent;
import java.util.List;
import checkmo.notification.internal.entity.Notification;
import checkmo.notification.internal.entity.Notification.NotificationType;
import checkmo.notification.internal.exception.NotificationErrorStatus;
import checkmo.notification.internal.exception.NotificationException;
import checkmo.notification.internal.repository.NotificationRepository;
import checkmo.notification.internal.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandService {

    private final ClubManagementAPI clubManagementAPI;

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    private final CacheManager cacheManager;

    /**
     * 좋아요 알림 생성
     *
     * @param event 좋아요 알림 정보 DTO
     */
    public void createNotification(BookStoryEvent.BookStoryLiked event) {
        NotificationType type = NotificationType.LIKE;
        if (!isNotificationEnabled(event.receiverId(), type)) {
            return;
        }

        Long sourceId = event.eventId();
        if (notificationRepository.existsByNotificationTypeAndSourceId(type, sourceId)) {
            return;
        }

        Notification notification = Notification.builder()
                .notificationType(type)
                .sourceId(sourceId)
                .domainId(event.bookStoryId())
                .senderId(event.senderId())
                .receiverId(event.receiverId())
                .build();
        try {
            notificationRepository.save(notification);
            evictNotificationCache(event.receiverId());
        } catch (DataIntegrityViolationException e) {
            // 다른 인스턴스가 동일 알람을 저장한 경우 -> 무시
        }
    }

    /**
     * 댓글 알림 생성
     *
     * @param event 댓글 알림 정보 DTO
     */
    public void createNotification(BookStoryEvent.BookStoryComment event) {
        NotificationType type = NotificationType.COMMENT;
        if (!isNotificationEnabled(event.receiverId(), type)) {
            return;
        }

        Long sourceId = event.eventId();
        if (notificationRepository.existsByNotificationTypeAndSourceId(type, sourceId)) {
            return;
        }

        Notification notification = Notification.builder()
                .notificationType(type)
                .sourceId(sourceId)
                .domainId(event.bookStoryId())
                .senderId(event.senderId())
                .receiverId(event.receiverId())
                .build();
        try {
            notificationRepository.save(notification);
            evictNotificationCache(event.receiverId());
        } catch (DataIntegrityViolationException e) {
            // 다른 인스턴스가 동일 알람을 저장한 경우 -> 무시
        }
    }

    /**
     * 팔로우(구독) 알림 생성
     *
     * @param event 팔로우 알림 정보 DTO
     */
    public void createNotification(MemberEvent.Follow event) {
        Notification.NotificationType type = Notification.NotificationType.FOLLOW;
        if (!isNotificationEnabled(event.followingId(), type)) {
            return;
        }

        Long sourceId = event.eventId();
        if (notificationRepository.existsByNotificationTypeAndSourceId(type, sourceId)) {
            return;
        }

        // domainId = null, 프론트에서 displayName(닉네임)으로 프로필 페이지 접근
        Notification notification = Notification.builder()
                .notificationType(type)
                .sourceId(sourceId)
                .domainId(null)
                .senderId(event.followerId())
                .receiverId(event.followingId())
                .build();
        try {
            notificationRepository.save(notification);
            evictNotificationCache(event.followingId());
        } catch (DataIntegrityViolationException e) {
            // 다른 인스턴스가 동일 알람을 저장한 경우 -> 무시
        }
    }

    /**
     * 독서 클럽 가입 승인 알림 생성
     *
     * @param event 독서 클럽 가입 승인 알림 정보 DTO
     */
    public void createNotification(JoinClubEvent event) {
        Notification.NotificationType type = Notification.NotificationType.JOIN_CLUB;
        if (!isNotificationEnabled(event.memberId(), type)) {
            return;
        }

        Long sourceId = event.eventId();
        if (notificationRepository.existsByNotificationTypeAndSourceId(type, sourceId)) {
            return;
        }

        Notification notification = Notification.builder()
                .notificationType(type)
                .sourceId(sourceId)
                .domainId(event.clubId())
                .senderId("SYSTEM")
                .receiverId(event.memberId())
                .build();
        try {
            notificationRepository.save(notification);
            evictNotificationCache(event.memberId());
        } catch (DataIntegrityViolationException e) {
            // 다른 인스턴스가 동일 알람을 저장한 경우 -> 무시
        }
    }

    /**
     * 정기 모임 생성 알림 생성
     *
     * @param event 정기 모임 생성 알림 정보 DTO
     */
    public void createNotification(ClubMeetingCreated event) {
        NotificationType type = NotificationType.CLUB_MEETING_CREATED;
        Long sourceId = event.eventId();

        List<String> memberIds = clubManagementAPI.fetchActiveMemberIds(event.clubId());
        for (String memberId : memberIds) {
            createClubNotification(type, sourceId, event.clubId(), memberId);
        }
    }

    /**
     * 공지사항 생성 알림 생성
     *
     * @param event 공지사항 생성 알림 정보 DTO
     */
    public void createNotification(ClubNoticeCreated event) {
        NotificationType type = NotificationType.CLUB_NOTICE_CREATED;
        Long sourceId = event.eventId();

        List<String> memberIds = clubManagementAPI.fetchActiveMemberIds(event.clubId());
        for (String memberId : memberIds) {
            createClubNotification(type, sourceId, event.clubId(), memberId);
        }
    }

    private void createClubNotification(NotificationType type, Long sourceId, Long clubId,
                                        String receiverId) {
        if (!isNotificationEnabled(receiverId, type)) {
            return;
        }

        Notification notification = Notification.builder()
                .notificationType(type)
                .sourceId(sourceId)
                .domainId(clubId)
                .senderId("SYSTEM")
                .receiverId(receiverId)
                .build();
        try {
            notificationRepository.save(notification);
            evictNotificationCache(receiverId);
        } catch (DataIntegrityViolationException e) {
            // 다른 인스턴스가 동일 알람을 저장한 경우 -> 무시
        }
    }

    private void evictNotificationCache(String memberId) {
        Cache cache = cacheManager.getCache("notifications");
        if (cache != null) {
            cache.evict(memberId);
        }
    }

    private boolean isNotificationEnabled(String receiverId, NotificationType type) {
        return notificationSettingRepository.findByMemberId(receiverId)
                .map(setting -> setting.isEnabled(type))
                .orElse(true);
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
