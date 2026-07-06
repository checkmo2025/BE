package checkmo.notification.internal.service;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.member.MemberAPI;
import checkmo.notification.internal.converter.NotificationConverter;
import checkmo.notification.internal.entity.Notification;
import checkmo.notification.internal.service.query.NotificationQueryService;
import checkmo.notification.internal.service.query.NotificationSettingQueryService;
import checkmo.notification.web.dto.NotificationResponseDTO.BasicInfoList;
import checkmo.notification.web.dto.NotificationResponseDTO.BasicInfoPreviewList;
import checkmo.notification.web.dto.NotificationResponseDTO.SettingInfo;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryFacade {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int DEFAULT_PREVIEW_SIZE = 5;

    // Domain level 2
    private final MemberAPI memberAPI;
    private final ClubManagementAPI clubManagementAPI;

    private final NotificationQueryService notificationQueryService;
    private final NotificationSettingQueryService notificationSettingQueryService;

    @Cacheable(value = "notifications", key = "#memberId")
    public BasicInfoPreviewList retrieveNotificationPreviews(Long memberId) {
        List<Notification> notifications = notificationQueryService.retrieveUnreadNotifications(memberId, DEFAULT_PREVIEW_SIZE);

        Map<Long, String> senderNicknameMap = fetchSenderNicknameMap(notifications);
        Map<Long, String> clubNameMap = fetchClubNameMap(notifications);

        return NotificationConverter.toPreviewListDTO(notifications, senderNicknameMap, clubNameMap);
    }

    public BasicInfoList retrieveNotifications(Long memberId, Long cursorId) {
        CursorResult<Notification> notificationCursorResult = CursorPagingHelper.getPage(
                size -> notificationQueryService.retrieveNotifications(memberId, cursorId, size),
                Notification::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Notification> notifications = notificationCursorResult.content();

        Map<Long, String> senderNicknameMap = fetchSenderNicknameMap(notifications);
        Map<Long, String> clubNameMap = fetchClubNameMap(notifications);

        return NotificationConverter.toNotificationListDTO(
                notifications,
                senderNicknameMap,
                clubNameMap,
                notificationCursorResult,
                DEFAULT_PAGE_SIZE
        );
    }

    public SettingInfo retrieveNotificationSetting(Long memberId) {
        return notificationSettingQueryService.getNotificationSetting(memberId);
    }

    private Map<Long, String> fetchSenderNicknameMap(List<Notification> notifications) {
        List<Long> senderIds = notifications.stream()
                .filter(n -> !n.getNotificationType().isClubNotification())
                .map(Notification::getSenderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return memberAPI.fetchNicknameByMemberIds(senderIds);
    }

    private Map<Long, String> fetchClubNameMap(List<Notification> notifications) {
        List<Long> clubIds = notifications.stream()
                .filter(n -> n.getNotificationType().isClubNotification())
                .map(Notification::getDomainId)
                .distinct()
                .toList();
        return clubManagementAPI.fetchClubNamesByClubIds(clubIds);
    }
}
