package checkmo.domain.notification.handler;

import checkmo.domain.notification.service.command.NotificationCommandService;
import checkmo.event.FollowEvent;
import checkmo.event.LikeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationCommandService notificationCommandService;

    @Async("notificationExecutor")
    @TransactionalEventListener
    public void handleNotificationEvent(LikeEvent event) {
        // 이벤트 처리 로직
        notificationCommandService.createNotification(event);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener
    public void handleNotificationEvent(FollowEvent event) {
        // 이벤트 처리 로직
        notificationCommandService.createNotification(event);
    }
}
