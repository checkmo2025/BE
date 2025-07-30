package checkmo.domain.notification.service.query;

import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.domain.notification.converter.NotificationConverter;
import checkmo.domain.notification.entity.Notification;
import checkmo.domain.notification.repository.NotificationRepository;
import checkmo.domain.notification.web.dto.NotificationResponseDTO;
import checkmo.global.dto.NotificationSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final MemberQueryFacade memberQueryFacade;

    @Override
    public NotificationResponseDTO.NotificationListResponseDTO getNotifications(Long memberId, Long cursorId) {
        throw new UnsupportedOperationException("아직 구현 X - 커서 기반 페이징");
    }

    @Override
    public NotificationResponseDTO.NotificationListResponseDTO getNotifications(Long memberId, int size) {
        throw new UnsupportedOperationException("아직 구현 X");
    }

    @Cacheable(value = "notifications", key = "#receiverId")
    public NotificationSharedDTO.NotificationPreviewList getUnreadNotifications(String receiverId, int size) {
        // 알림 목록 조회
        List<Notification> notifications = notificationRepository
                .findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(receiverId, PageRequest.of(0, size));

        // 발신자 ID 목록 추출 (중복 제거)
        List<String> senderIds = notifications.stream()
                .map(Notification::getSenderId)
                .distinct()
                .toList();

        // 발신자 닉네임 배치 조회로 처리
        Map<String, String> senderNicknameMap = memberQueryFacade.getMemberNicknamesByMemberIds(senderIds);

        // DTO 변환
        return NotificationConverter.convertToPreviewListDTO(notifications, senderNicknameMap);
    }
}
