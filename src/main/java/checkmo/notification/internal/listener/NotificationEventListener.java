package checkmo.notification.internal.listener;

import checkmo.bookStory.BookStoryEvent;
import checkmo.clubManagement.ClubManagementEvent.JoinClubEvent;
import checkmo.member.MemberEvent;
import checkmo.notification.internal.service.command.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationEventListener {

    private final NotificationCommandService notificationCommandService;

    @ApplicationModuleListener
    public void handleNotificationEvent(BookStoryEvent.BookStoryLiked event) {
        try {
            notificationCommandService.createNotification(event);
        } catch (Exception e) {
            log.error("책이야기 좋아요 알림 생성 실패, LikeEvent: {}", event, e);
        }
    }

    @ApplicationModuleListener
    public void handleNotificationEvent(MemberEvent.Follow event) {
        try {
            notificationCommandService.createNotification(event);
        } catch (Exception e) {
            log.error("팔로우 알림 생성 실패, FollowEvent: {}", event, e);
        }
    }

    @ApplicationModuleListener
    public void handleNotificationEvent(JoinClubEvent event) {
        try {
            notificationCommandService.createNotification(event);
        } catch (Exception e) {
            log.error("독서 클럽 가입 승인 알림 생성 실패, JoinClubEvent: {}", event, e);
        }
    }
}
