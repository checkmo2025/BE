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

    //TODO : 나중에 구현할 때 주석 해제하고 사용하기!!
    //private final NotificationCommandService notificationCommandService;

    @Async("notificationExecutor")
    @TransactionalEventListener
    public void handleNotificationEvent(FollowEvent event) {
        // 이벤트 처리 로직

    }

    @Async("notificationExecutor")
    @TransactionalEventListener
    public void handleNotificationEvent(LikeEvent event) {
        // 이벤트 처리 로직

    }
}
