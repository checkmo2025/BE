package checkmo.clubMeeting.web.dto.bookshelf;

import static checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;

import checkmo.book.BookExternalDTO;
import checkmo.member.MemberExternalDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
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
        private MembershipInfo membershipInfo;
        private boolean hasNext;
        private Long nextCursor;
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
        private int generation;
        private String tag;
        private double averageRate;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookShelfDetail {
        private MembershipInfo membershipInfo;
        private MeetingInfo meetingInfo;
        private BookExternalDTO.DetailInfo bookDetailInfo;
        private TopicList topicList;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookReviewList {
        private List<BookReviewDetail> bookReviewDetailList;
        private MembershipInfo membershipInfo;
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
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicList {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private MembershipInfo membershipInfo;
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
        private boolean isAuthor;
    }

}
