package checkmo.domain.notification.facade;

import checkmo.member.facade.MemberQueryFacade;
import checkmo.domain.notification.converter.NotificationConverter;
import checkmo.domain.notification.entity.Notification;
import checkmo.domain.notification.service.query.NotificationQueryService;
import checkmo.domain.notification.web.dto.NotificationResponseDTO;
import checkmo.global.dto.NotificationSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryFacadeImpl implements NotificationQueryFacade {

    // 페이징 기본 크기 상수
    public static final int DEFAULT_PAGE_SIZE = 20;

    // Domain level 2
    private final MemberQueryFacade memberQueryFacade;

    // 자신의 QueryService
    private final NotificationQueryService notificationQueryService;

    @Override
    @Cacheable(value = "notifications", key = "#memberId")
    public NotificationSharedDTO.NotificationPreviewList getNotificationPreviewList(String memberId, int size) {
        // 1. Service에서 순수 엔티티 조회
        List<Notification> notifications = notificationQueryService.findUnreadNotifications(memberId, size);

        // 2. 발신자 ID 목록 추출 (중복 제거)
        List<String> senderIds = notifications.stream()
                .map(Notification::getSenderId)
                .distinct()
                .toList();

        // 3. 발신자 닉네임 배치 조회로 처리
        Map<String, String> senderNicknameMap = memberQueryFacade.getMemberNicknamesByMemberIds(senderIds);

        // 4. DTO 변환
        return NotificationConverter.convertToPreviewListDTO(notifications, senderNicknameMap);
    }

    @Override
    public NotificationResponseDTO.NotificationListResponse getNotifications(String memberId, Long cursorId) {
        // 1. 알림 목록 조회
        List<Notification> notifications = notificationQueryService.findNotifications(memberId, cursorId, DEFAULT_PAGE_SIZE + 1);

        // 2. Facade에서 페이지네이션 로직 처리
        boolean hasNext = notifications.size() > DEFAULT_PAGE_SIZE;
        Long nextCursor = null;
        if (hasNext) {
            notifications.removeLast();
            nextCursor = notifications.getLast().getId();
        }

        // 3. 알림을 보낸 사람의 ID 목록 가져오기
        List<String> senderIds = notifications.stream()
                .map(Notification::getSenderId)
                .distinct()
                .toList();

        // 4. 알림 보낸 사람 닉네임 배치 조회
        Map<String, String> senderNicknameMap = memberQueryFacade.getMemberNicknamesByMemberIds(senderIds);

        // 5. DTO 변환
        return NotificationConverter.convertToNotificationListDTO(
                notifications, 
                senderNicknameMap,
                hasNext, 
                nextCursor, 
                DEFAULT_PAGE_SIZE
        );
    }
}
