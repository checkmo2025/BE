package checkmo.notification.facade;

import checkmo.notification.internal.service.command.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandFacadeImpl implements NotificationCommandFacade {

    // 자신의 CommandService
    private final NotificationCommandService notificationCommandService;

    @Override
    public void markNotificationAsRead(Long notificationId, String memberId) {
        notificationCommandService.markNotificationAsRead(notificationId, memberId);
    }
}
