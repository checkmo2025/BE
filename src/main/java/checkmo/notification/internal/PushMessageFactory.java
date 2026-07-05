package checkmo.notification.internal;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.infra.PushSendRequest;
import checkmo.member.MemberAPI;
import checkmo.notification.internal.entity.Notification;
import checkmo.notification.internal.entity.Notification.NotificationType;
import checkmo.notification.internal.entity.PushDelivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushMessageFactory {

    private static final String APP_TITLE = "책모";
    private static final String SOUND = "default";
    private static final String PRIORITY = "high";
    private static final String CHANNEL_ID = "checkmo-default";

    private final MemberAPI memberAPI;
    private final ClubManagementAPI clubManagementAPI;

    public PushSendRequest build(PushDelivery delivery, Notification notification) {
        String displayName = resolveDisplayName(notification);
        return new PushSendRequest(
                delivery.getPushDevice().getExpoPushToken(),
                APP_TITLE,
                buildBody(notification.getNotificationType(), displayName),
                buildData(notification, displayName),
                SOUND,
                PRIORITY,
                CHANNEL_ID
        );
    }

    private String resolveDisplayName(Notification notification) {
        return switch (notification.getNotificationType()) {
            case LIKE, COMMENT, FOLLOW -> fetchNickname(notification.getSenderId());
            case JOIN_CLUB, CLUB_MEETING_CREATED, CLUB_NOTICE_CREATED -> fetchClubName(notification.getDomainId());
        };
    }

    private String buildBody(NotificationType type, String displayName) {
        return switch (type) {
            case LIKE -> displayName + "님이 회원님의 책이야기를 좋아합니다.";
            case COMMENT -> displayName + "님이 회원님의 책이야기에 댓글을 달았습니다.";
            case FOLLOW -> displayName + "님이 회원님을 팔로우했습니다.";
            case JOIN_CLUB -> displayName + " 모임 가입이 승인되었습니다.";
            case CLUB_MEETING_CREATED -> displayName + " 모임에 새로운 정기 모임이 생성되었습니다.";
            case CLUB_NOTICE_CREATED -> displayName + " 모임에 새로운 공지사항이 등록되었습니다.";
        };
    }

    private Map<String, Object> buildData(Notification notification, String displayName) {
        Map<String, Object> data = new HashMap<>();
        data.put("schemaVersion", 1);
        data.put("notificationId", notification.getId());
        data.put("notificationType", notification.getNotificationType().name());
        data.put("domainId", notification.getDomainId());
        data.put("displayName", displayName);

        // CLUB_MEETING_CREATED, CLUB_NOTICE_CREATED만 sourceId 포함
        if (notification.getNotificationType().needsSourceId()) {
            data.put("sourceId", notification.getSourceId());
        }

        return data;
    }

    private String fetchNickname(String memberId) {
        try {
            return memberAPI.fetchNickname(memberId);
        } catch (Exception e) {
            log.debug("닉네임 조회 실패 (탈퇴한 회원): memberId={}", memberId);
            return "탈퇴한 회원";
        }
    }

    private String fetchClubName(Long clubId) {
        try {
            return clubManagementAPI.fetchClubName(clubId);
        } catch (Exception e) {
            log.debug("모임명 조회 실패 (삭제된 모임): clubId={}", clubId);
            return "삭제된 모임";
        }
    }
}
