package checkmo.realtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class RealtimeExternalDTO {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamChatReportInfo {
        private Long chatMessageId;
        private Long clubId;
        private Long meetingId;
        private Long teamId;
    }
}
