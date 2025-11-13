package checkmo.notification.internal.service.command;

import checkmo.bookStory.LikeEvent;
import checkmo.clubMeeting.JoinClubEvent;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.FollowEvent;
import checkmo.member.MemberAPI;
import checkmo.notification.internal.converter.NotificationConverter;
import checkmo.notification.internal.entity.Notification;
import checkmo.notification.internal.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
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
    @CacheEvict(value = "notifications", key = "#event.getReceiverId()")
    public void createNotification(LikeEvent event) {

        // 리다이렉트 경로를 생성
        String redirectPath = NotificationConverter.getRedirectPath(Notification.NotificationType.LIKE, event.getBookStoryId());

        // Notification 객체를 생성하고 저장 (targetName = null)
        Notification notification = NotificationConverter.fromEvent(
                Notification.NotificationType.LIKE,
                redirectPath,
                null,
                event.getSenderId(),  // 좋아요를 누른 사람
                event.getReceiverId() // 좋아요를 받은 사람
        );
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    @CacheEvict(value = "notifications", key = "#event.getFollowingId()")
    public void createNotification(FollowEvent event) {

        // 팔로우 누른 사람의 닉네임을 가져옴
        String FollowerNickname = memberAPI.getMemberNicknameById(event.getFollowerId());

        // 리다이렉트 경로를 생성
        String redirectPath = NotificationConverter.getRedirectPath(Notification.NotificationType.FOLLOW,
                FollowerNickname);

        // Notification 객체를 생성하고 저장 (targetName = followerNickname)
        Notification notification = NotificationConverter.fromEvent(
                Notification.NotificationType.FOLLOW,
                redirectPath,
                FollowerNickname,
                event.getFollowerId(), // 팔로우 누른 사람
                event.getFollowingId() // 팔로우 당하는 사람
        );
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    @CacheEvict(value = "notifications", key = "#event.getMemberId()")
    public void createNotification(JoinClubEvent event) {

        // 리다이렉트 경로를 생성
        String redirectPath = NotificationConverter.getRedirectPathForClub(Notification.NotificationType.JOIN_CLUB,
                event.getClubId());

        // Notification 객체를 생성하고 저장 (sender 없이, targetName 포함)
        Notification notification = NotificationConverter.fromEvent(
                Notification.NotificationType.JOIN_CLUB,
                redirectPath,
                event.getClubName(),
                null, // 시스템 알림이므로 sender는 null
                event.getMemberId() // 독서 클럽 가입 승인 이벤트에서 멤버의 ID를 가져옴 (새로 가입 된 사람)
        );
        notificationRepository.save(notification);
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
