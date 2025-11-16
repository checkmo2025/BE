package checkmo.notification.internal.service.command;

import checkmo.bookStory.BookStoryEvent;
import checkmo.clubManagement.ClubManagementEvent.JoinClubEvent;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.MemberAPI;
import checkmo.member.MemberEvent;
import checkmo.notification.internal.converter.NotificationConverter;
import checkmo.notification.internal.entity.Notification;
import checkmo.notification.internal.entity.Notification.NotificationType;
import checkmo.notification.internal.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationCommandServiceImpl implements NotificationCommandService {

    // Domain level 2
    private final MemberAPI memberAPI;

    // 자신의 Repository
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    @CacheEvict(value = "notifications", key = "#event.receiverId()")
    public void createNotification(BookStoryEvent.BookStoryLiked event) {
        NotificationType type = NotificationType.LIKE;
        Long sourceId = event.eventId();
        if (notificationRepository.existsByNotificationTypeAndSourceId(type, sourceId)) {
            return;
        }

        // 리다이렉트 경로를 생성
        String redirectPath = NotificationConverter.getRedirectPath(type, event.bookStoryId());

        // Notification 객체를 생성하고 저장 (targetName = null)
        Notification notification = NotificationConverter.fromEvent(
                type,
                sourceId,
                redirectPath,
                null,
                event.senderId(),  // 좋아요를 누른 사람
                event.receiverId() // 좋아요를 받은 사람
        );
        try {
            notificationRepository.save(notification);
        } catch (DataIntegrityViolationException e) {
            // 다른 인스턴스가 동일 알람을 저장한 경우 -> 무시
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "notifications", key = "#event.followingId()")
    public void createNotification(MemberEvent.Follow event) {
        Notification.NotificationType type = Notification.NotificationType.FOLLOW;
        Long sourceId = event.eventId();
        if (notificationRepository.existsByNotificationTypeAndSourceId(type, sourceId)) {
            return;
        }

        // 팔로우 누른 사람의 닉네임을 가져옴
        String FollowerNickname = memberAPI.getMemberNicknameById(event.followerId());

        // 리다이렉트 경로를 생성
        String redirectPath = NotificationConverter.getRedirectPath(type, FollowerNickname);

        // Notification 객체를 생성하고 저장 (targetName = followerNickname)
        Notification notification = NotificationConverter.fromEvent(
                type,
                sourceId,
                redirectPath,
                FollowerNickname,
                event.followerId(), // 팔로우 누른 사람
                event.followingId() // 팔로우 당하는 사람
        );
        try {
            notificationRepository.save(notification);
        } catch (DataIntegrityViolationException e) {
            // 다른 인스턴스가 동일 알람을 저장한 경우 -> 무시
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "notifications", key = "#event.memberId()")
    public void createNotification(JoinClubEvent event) {
        Notification.NotificationType type = Notification.NotificationType.JOIN_CLUB;
        Long sourceId = event.eventId();
        if (notificationRepository.existsByNotificationTypeAndSourceId(type, sourceId)) {
            return;
        }

        // 리다이렉트 경로를 생성
        String redirectPath = NotificationConverter.getRedirectPathForClub(Notification.NotificationType.JOIN_CLUB,
                event.clubId());

        // Notification 객체를 생성하고 저장 (sender 없이, targetName 포함)
        Notification notification = NotificationConverter.fromEvent(
                Notification.NotificationType.JOIN_CLUB,
                event.eventId(),
                redirectPath,
                event.clubName(),
                null, // 시스템 알림이므로 sender는 null
                event.memberId() // 독서 클럽 가입 승인 이벤트에서 멤버의 ID를 가져옴 (새로 가입 된 사람)
        );
        try {
            notificationRepository.save(notification);
        } catch (DataIntegrityViolationException e) {
            // 다른 인스턴스가 동일 알람을 저장한 경우 -> 무시
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "notifications", key = "#memberId")
    public void markNotificationAsRead(Long notificationId, String memberId) {
        // 멤버 ID와 알림 ID로 알림을 조회
        Notification notification = notificationRepository.findByIdAndReceiverId(notificationId, memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOTIFICATION_NOT_FOUND));

        // 알림이 이미 읽음 상태인지 확인
        if (notification.isRead()) {
            throw new GeneralException(ErrorStatus.NOTIFICATION_ALREADY_READ);
        }

        // 알림을 읽음 상태로 변경
        notification.markAsRead();
    }
}
