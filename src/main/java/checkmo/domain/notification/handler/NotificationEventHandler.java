package checkmo.domain.notification.handler;

import checkmo.domain.notification.service.command.NotificationCommandService;
import checkmo.event.FollowEvent;
import checkmo.event.JoinClubEvent;
import checkmo.event.LikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationCommandService notificationCommandService;

    @Async("notificationExecutor")
    @TransactionalEventListener
    public void handleNotificationEvent(LikeEvent event) {
        try {
            notificationCommandService.createNotification(event);
        } catch (Exception e) {
            log.error("책이야기 좋아요 알림 생성 실패, LikeEvent: {}", event, e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener
    public void handleNotificationEvent(FollowEvent event) {
        try {
            notificationCommandService.createNotification(event);
        } catch (Exception e) {
            log.error("팔로우 알림 생성 실패, FollowEvent: {}", event, e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener
    public void handleNotificationEvent(JoinClubEvent event) {
        try {
            notificationCommandService.createNotification(event);
        } catch (Exception e) {
            log.error("독서 클럽 가입 승인 알림 생성 실패, JoinClubEvent: {}", event, e);
        }
    }
}
