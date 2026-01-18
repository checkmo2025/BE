package checkmo.notification.internal.service.command;

import checkmo.notification.internal.entity.NotificationSetting;
import checkmo.notification.web.dto.NotificationSettingType;
import checkmo.notification.internal.exception.NotificationErrorStatus;
import checkmo.notification.internal.exception.NotificationException;
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

    public void toggleNotificationSetting(String memberId, NotificationSettingType settingType) {
        NotificationSetting setting = notificationSettingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new NotificationException(NotificationErrorStatus.NOTIFICATION_SETTING_NOT_FOUND));

        switch (settingType) {
            case BOOK_STORY_LIKED -> setting.toggleBookStoryLiked();
            case BOOK_STORY_COMMENT -> setting.toggleBookStoryComment();
            case CLUB_NOTICE_CREATED -> setting.toggleClubNoticeCreated();
            case CLUB_MEETING_CREATED -> setting.toggleClubMeetingCreated();
        }
    }
}