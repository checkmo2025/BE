package checkmo.notification.internal.handler;

import checkmo.bookStory.LikeEvent;
import checkmo.clubManagement.ClubManagementEvent.JoinClubEvent;
import checkmo.member.MemberEvent;
import checkmo.notification.internal.service.command.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationCommandService notificationCommandService;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(LikeEvent event) {
        try {
            notificationCommandService.createNotification(event);
        } catch (Exception e) {
            log.error("책이야기 좋아요 알림 생성 실패, LikeEvent: {}", event, e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(MemberEvent.Follow event) {
        try {
            notificationCommandService.createNotification(event);
        } catch (Exception e) {
            log.error("팔로우 알림 생성 실패, FollowEvent: {}", event, e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(JoinClubEvent event) {
        try {
            notificationCommandService.createNotification(event);
        } catch (Exception e) {
            log.error("독서 클럽 가입 승인 알림 생성 실패, JoinClubEvent: {}", event, e);
        }
    }
}
