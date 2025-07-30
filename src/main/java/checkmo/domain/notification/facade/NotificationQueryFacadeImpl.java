package checkmo.domain.notification.facade;

import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.domain.notification.converter.NotificationConverter;
import checkmo.domain.notification.entity.Notification;
import checkmo.domain.notification.service.query.NotificationQueryService;
import checkmo.domain.notification.web.dto.NotificationResponseDTO;
import checkmo.global.dto.NotificationSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryFacadeImpl implements NotificationQueryFacade {

    public static final int DEFAULT_PAGE_SIZE = 20;

    private final NotificationQueryService notificationQueryService;
    private final MemberQueryFacade memberQueryFacade;

    @Override
    public NotificationSharedDTO.NotificationPreviewList getNotificationPreviewList(String memberId, int size) {
        return notificationQueryService.getUnreadNotifications(memberId, size);
    }

    @Override
    public NotificationResponseDTO.NotificationListResponseDTO getNotifications(String memberId, Long cursorId) {
        // 1. 알림 목록 조회
        List<Notification> notifications = notificationQueryService.findNotifications(memberId, cursorId, DEFAULT_PAGE_SIZE + 1);

        // 2. Facade에서 페이지네이션 로직 처리
        boolean hasNext = notifications.size() > DEFAULT_PAGE_SIZE;
        Long nextCursor = null;
        if (hasNext) {
            notifications.removeLast();
            nextCursor = notifications.getLast().getId();
        }

        // 3. Service를 통해 나머지 정보들을 가져옴
        List<String> senderIds = notifications.stream()
                .map(Notification::getSenderId)
                .distinct()
                .toList();
                
        List<String> receiverIds = notifications.stream()
                .map(Notification::getReceiverId)
                .distinct()
                .toList();

        // 4. 발신자/수신자 닉네임 배치 조회
        Map<String, String> senderNicknameMap = memberQueryFacade.getMemberNicknamesByMemberIds(senderIds);
        Map<String, String> receiverNicknameMap = memberQueryFacade.getMemberNicknamesByMemberIds(receiverIds);

        // 5. DTO 변환
        return NotificationConverter.convertToNotificationListDTO(
                notifications, 
                senderNicknameMap, 
                receiverNicknameMap, 
                hasNext, 
                nextCursor, 
                DEFAULT_PAGE_SIZE
        );
    }
}
