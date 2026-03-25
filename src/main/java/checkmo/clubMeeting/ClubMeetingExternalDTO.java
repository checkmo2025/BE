package checkmo.clubMeeting;

import checkmo.book.BookExternalDTO;
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
        private Integer generation;
        private String tag;
        private double averageRate;
        private BookExternalDTO.BasicInfo bookInfo;
    }

    public record ToggleTopicResult(boolean isSelected, Failure failure) {
        public boolean isSuccess() {
            return failure == Failure.NONE;
        }

        public enum Failure {
            NONE,
            TEAM_NOT_FOUND,
            TOPIC_NOT_FOUND,
            INTERNAL_ERROR
        }
    }
}
