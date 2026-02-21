package checkmo.clubMeeting.web.dto.bookshelf;

import checkmo.book.BookExternalDTO;
import checkmo.member.MemberExternalDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookShelfResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookShelfList {
        private List<BookShelfInfo> bookShelfInfoList;
        private boolean hasNext;
        private Long nextCursor;

        @Getter(AccessLevel.NONE)
        private boolean staff;

        @JsonProperty("isStaff")
        public boolean isStaff() {
            return staff;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookShelfInfo {
        private MeetingInfo meetingInfo;
        private BookExternalDTO.BasicInfo bookInfo;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingInfo {
        private Long meetingId;
        private Integer generation;
        private String tag;
        private double averageRate;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookShelfDetail {
        private MeetingInfo meetingInfo;
        private BookExternalDTO.DetailInfo bookDetailInfo;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookReviewList {
        private List<BookReviewDetail> bookReviewDetailList;
        private boolean hasNext;
        private Long nextCursor;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookReviewDetail {
        private Long bookReviewId;
        private String description;
        private double rate;
        private MemberExternalDTO.BasicInfo authorInfo;
        @Getter(AccessLevel.NONE)
        private boolean author;

        @JsonProperty("isAuthor")
        public boolean isAuthor() {
            return author;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicList {
        private List<TopicDetail> topicDetailList;
        private boolean hasNext;
        private Long nextCursor;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicDetail {
        private Long topicId;
        private String content;
        private MemberExternalDTO.BasicInfo authorInfo;
        @Getter(AccessLevel.NONE)
        private boolean author;

        @JsonProperty("isAuthor")
        public boolean isAuthor() {
            return author;
        }
    }

}
