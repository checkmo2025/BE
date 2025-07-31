package checkmo.domain.notification.facade;

import checkmo.domain.notification.service.command.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandFacadeImpl implements NotificationCommandFacade {

    private final NotificationCommandService notificationCommandService;

    @Override
    public void markNotificationAsRead(Long notificationId, String memberId) {
        notificationCommandService.markNotificationAsRead(notificationId, memberId);
    }
}
