package checkmo.realtime.internal.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RealtimeErrorStatus {
    // 공통 오류 - Authorization
    INTERNAL_SERVER_ERROR("RTM_000", "서버 내부 오류가 발생했습니다."),

    MEETING_NOT_IN_CLUB("RTM_001", "모임이 클럽에 속하지 않습니다."),
    TEAM_NOT_IN_ClUB("RTM_002", "팀이 클럽에 속하지 않습니다."),
    NOT_ACTIVE_CLUB_MEMBER("RTM_003", "활동 중인 클럽 회원이 아닙니다."),
    NOT_TEAM_MEMBER_OR_STAFF("RTM_004", "팀 멤버 또는 운영진만 접근할 수 있습니다."),

    UNAUTHENTICATED("RTM_005", "인증되지 않은 사용자입니다."),
    INVALID_DESTINATION("RTM_006", "유효하지 않은 목적지입니다."),
    MISSING_DESTINATION("RTM_007", "목적지가 누락되었습니다."),

    // 채팅 관련 오류
    MESSAGE_NOT_FOUND("CHAT_001", "채팅 메시지를 찾을 수 없습니다."),
    CHAT_DISABLED("CHAT_002", "현재 모임에서는 채팅이 불가능합니다."),

    // 발제 관련 오류
    TOPIC_NOT_FOUND("TOPIC_001", "발제를 찾을 수 없습니다.");

    private final String code;
    private final String message;
}
