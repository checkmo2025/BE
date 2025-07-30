package checkmo.domain.notification.facade;

import checkmo.domain.notification.service.query.NotificationQueryService;
import checkmo.domain.notification.web.dto.NotificationResponseDTO;
import checkmo.global.dto.NotificationSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryFacadeImpl implements NotificationQueryFacade {

    private final NotificationQueryService notificationQueryService;

    @Override
    public NotificationSharedDTO.NotificationPreviewList getNotificationPreviewList(String memberId, int size) {
        return notificationQueryService.getUnreadNotifications(memberId, size);
    }

    @Override
    public NotificationResponseDTO.NotificationListResponseDTO getNotifications(Long memberId, Long cursorId) {
        return notificationQueryService.getNotifications(memberId, cursorId);
    }
}
