package checkmo.clubMeeting;

import checkmo.book.BookExternalDTO;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 클럽 모임 모듈과 관련된 다른 모듈에게 public한 DTO 클래스
 */
public class ClubMeetingExternalDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailInfo {
        private Long meetingId;
        private String title;
        private LocalDateTime meetingTime;
        private String location;
        private int generation;
        private String tag;
        private String content;
        private BookExternalDTO.BasicInfo bookInfo;
    }

}
