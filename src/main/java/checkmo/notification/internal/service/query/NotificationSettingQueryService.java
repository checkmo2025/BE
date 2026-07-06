package checkmo.notification.internal.service.query;

import checkmo.notification.internal.entity.NotificationSetting;
import checkmo.notification.internal.exception.NotificationErrorStatus;
import checkmo.notification.internal.exception.NotificationException;
import checkmo.notification.internal.repository.NotificationSettingRepository;
import checkmo.notification.web.dto.NotificationResponseDTO.SettingInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingQueryService {

    private final NotificationSettingRepository notificationSettingRepository;

    public SettingInfo getNotificationSetting(Long memberId) {
        NotificationSetting setting = notificationSettingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new NotificationException(NotificationErrorStatus.NOTIFICATION_SETTING_NOT_FOUND));

        return SettingInfo.builder()
                .bookStoryLiked(setting.isBookStoryLiked())
                .bookStoryComment(setting.isBookStoryComment())
                .clubNoticeCreated(setting.isClubNoticeCreated())
                .clubMeetingCreated(setting.isClubMeetingCreated())
                .newFollower(setting.isNewFollower())
                .joinClub(setting.isJoinClub())
                .build();
    }
}
