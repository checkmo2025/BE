package checkmo.notification.internal.service.command;

import checkmo.notification.internal.entity.NotificationSetting;
import checkmo.notification.internal.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationSettingCommandService {

    private final NotificationSettingRepository notificationSettingRepository;

    public void createNotificationSetting(String memberId) {
        if (notificationSettingRepository.existsByMemberId(memberId)) {
            return;
        }

        NotificationSetting notificationSetting = NotificationSetting.builder()
                .memberId(memberId)
                .build();

        notificationSettingRepository.save(notificationSetting);
    }
}