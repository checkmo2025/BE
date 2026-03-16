package checkmo.realtime.web.dto;

import checkmo.member.MemberExternalDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResponseDTO {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatHistoryList {
        private List<Chat> chats;
        private boolean hasNext;
        private Long nextCursor;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Chat {
        private MemberExternalDTO.BasicInfo sender;
        private Long messageId;
        private String content;
        private LocalDateTime sendAt;
    }

}
