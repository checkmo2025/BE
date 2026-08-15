package checkmo.bookStory.web.dto;

import checkmo.book.BookExternalDTO;
import checkmo.bookStory.internal.entity.BookStoryStatus;
import checkmo.member.MemberExternalDTO;
import checkmo.member.MemberExternalDTO.BasicInfoWithFollow;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookStoryResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SitemapPage {
        private List<SitemapItem> items;
        private boolean hasNext;
        private Long nextCursor;
        private int pageSize;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SitemapItem {
        private Long id;
        private LocalDateTime updatedAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookStoryList {
        private List<BasicInfo> basicInfoList;
        private boolean hasNext;
        private Long nextCursor;
        private int pageSize;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BasicInfo {
        private Long bookStoryId;
        private BookExternalDTO.BasicInfo bookInfo;
        private BasicInfoWithFollow authorInfo;
        private String bookStoryTitle;
        private String description;
        private List<String> imageUrls;
        private int likes;
        private BookStoryStatus status;
        private boolean canContinue;

        private boolean likedByMe; // 내가 좋아요를 눌렀는지 여부 (true: 눌렀음, false: 안누름)

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdAt;

        private boolean writtenByMe; // 작성자가 본인인지 여부 (true: 본인, false: 타인)
        private int commentCount;
        private int viewCount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailInfo {
        private Long bookStoryId;
        private BookExternalDTO.BasicInfo bookInfo;
        private BasicInfoWithFollow authorInfo;
        private String bookStoryTitle;
        private String description;
        private List<String> imageUrls;
        private int likes;
        private BookStoryStatus status;
        private boolean canContinue;

        private boolean likedByMe; // 내가 좋아요를 눌렀는지 여부 (true: 눌렀음, false: 안누름)

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdAt;

        private boolean writtenByMe; // 작성자가 본인인지 여부 (true: 본인, false: 타인)
        private int viewCount;
        private int commentCount; // 댓글 전체 개수 (대댓글 포함)

        private List<CommentInfo> comments;

        private Long prevBookStoryId;
        private Long nextBookStoryId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminBookStoryList {
        private List<AdminBasicInfo> basicInfoList;
        private int page;
        private int pageSize;
        private int totalPages;
        private long totalElements;
        private boolean hasNext;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminBasicInfo {
        private Long bookStoryId;
        private String bookStoryTitle;
        private String authorEmail;
        private String authorNickname;
        private String bookTitle;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
        private LocalDateTime createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommentInfo {
        private Long commentId;
        private String content;
        private List<String> imageUrls;
        private MemberExternalDTO.BasicInfo authorInfo;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdAt;

        private boolean writtenByMe; // 작성자가 본인인지 여부
        private boolean deleted; // 삭제된 댓글인지 여부
        private List<CommentInfo> replies; // 대댓글 목록
    }
}
