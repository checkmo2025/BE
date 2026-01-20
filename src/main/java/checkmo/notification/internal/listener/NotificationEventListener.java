package checkmo.notification.internal.listener;

import checkmo.bookStory.BookStoryEvent;
import checkmo.clubManagement.ClubManagementEvent.JoinClubEvent;
import checkmo.clubMeeting.ClubMeetingEvent.ClubMeetingCreated;
import checkmo.clubNotice.ClubNoticeEvent.ClubNoticeCreated;
import checkmo.member.MemberEvent;
import checkmo.notification.internal.service.command.NotificationCommandService;
import checkmo.notification.internal.service.command.NotificationSettingCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationEventListener {

    private final NotificationCommandService notificationCommandService;
    private final NotificationSettingCommandService notificationSettingCommandService;

    @ApplicationModuleListener
    public void handleNotificationEvent(BookStoryEvent.BookStoryLiked event) {
        try {
            notificationCommandService.createNotification(event);
        } catch (Exception e) {
            log.error("책이야기 좋아요 알림 생성 실패, LikeEvent: {}", event, e);
            throw e;
        }
    }

    @ApplicationModuleListener
    public void handleNotificationEvent(BookStoryEvent.BookStoryComment event) {
        try {
            notificationCommandService.createNotification(event);
        } catch (Exception e) {
            log.error("책이야기 댓글 알림 생성 실패, CommentEvent: {}", event, e);
            throw e;
        }
    }

    @ApplicationModuleListener
    public void handleNotificationEvent(MemberEvent.Follow event) {
        try {
            notificationCommandService.createNotification(event);
        } catch (Exception e) {
            log.error("팔로우 알림 생성 실패, FollowEvent: {}", event, e);
            throw e;
        }
    }

    @ApplicationModuleListener
    public void handleNotificationEvent(JoinClubEvent event) {
        try {
            notificationCommandService.createNotification(event);
        } catch (Exception e) {
            log.error("독서 클럽 가입 승인 알림 생성 실패, JoinClubEvent: {}", event, e);
            throw e;
        }
    }

    @ApplicationModuleListener
    public void handleNotificationEvent(ClubMeetingCreated event) {
        try {
            notificationCommandService.createNotification(event);
        } catch (Exception e) {
            log.error("정기 모임 생성 알림 생성 실패, ClubMeetingCreated: {}", event, e);
            throw e;
        }
    }

    @ApplicationModuleListener
    public void handleNotificationEvent(ClubNoticeCreated event) {
        try {
            notificationCommandService.createNotification(event);
        } catch (Exception e) {
            log.error("공지사항 생성 알림 생성 실패, ClubNoticeCreated: {}", event, e);
            throw e;
        }
    }

    @ApplicationModuleListener
    public void handleMemberRegistrationCompleted(MemberEvent.MemberRegistrationCompleted event) {
        try {
            notificationSettingCommandService.createNotificationSetting(event.memberId());
        } catch (Exception e) {
            log.error("알림 설정 생성 실패, MemberRegistrationCompleted: {}", event, e);
            throw e;
        }
    }
}
