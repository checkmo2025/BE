package checkmo.notification.internal.service;

import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.common.template.ExtractHelper;
import checkmo.member.MemberAPI;
import checkmo.notification.internal.converter.NotificationConverter;
import checkmo.notification.internal.entity.Notification;
import checkmo.notification.internal.service.query.NotificationQueryService;
import checkmo.notification.web.dto.NotificationResponseDTO.BasicInfoList;
import checkmo.notification.web.dto.NotificationResponseDTO.BasicInfoPreviewList;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryFacade {

    public static final int DEFAULT_PAGE_SIZE = 20;

    // Domain level 2
    private final MemberAPI memberAPI;

    private final NotificationQueryService notificationQueryService;

    @Cacheable(value = "notifications", key = "#memberId")
    public BasicInfoPreviewList retrieveNotificationPreviews(String memberId, int size) {
        List<Notification> notifications = notificationQueryService.retrieveUnreadNotifications(memberId, size);

        List<String> senderIds = ExtractHelper.extractDistinctList(notifications, Notification::getSenderId);

        // 발신자 닉네임 배치 조회로 처리
        Map<String, String> senderNicknameMap = memberAPI.fetchNicknameByMemberIds(senderIds);

        return NotificationConverter.convertToPreviewListDTO(notifications, senderNicknameMap);
    }

    public BasicInfoList retrieveNotifications(String memberId, Long cursorId) {
        CursorResult<Notification> notificationCursorResult = CursorPagingHelper.getPage(
                size -> notificationQueryService.retrieveNotifications(memberId, cursorId, size),
                Notification::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Notification> notifications = notificationCursorResult.content();

        List<String> senderIds = ExtractHelper.extractDistinctList(notifications, Notification::getSenderId);

        // 알림 보낸 사람 닉네임 배치 조회
        Map<String, String> senderNicknameMap = memberAPI.fetchNicknameByMemberIds(senderIds);

        return NotificationConverter.convertToNotificationListDTO(
                notifications,
                senderNicknameMap,
                notificationCursorResult,
                DEFAULT_PAGE_SIZE
        );
    }

}
