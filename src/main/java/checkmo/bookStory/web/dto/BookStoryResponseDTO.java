package checkmo.bookStory.web.dto;

import static checkmo.clubManagement.ClubManagementExternalDTO.ClubList;

import checkmo.book.BookExternalDTO;
import checkmo.clubManagement.ClubManagementExternalDTO;
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
    public static class BookStoryList {
        private ScopeInfo scopeInfo;    // 현재 선택된 범위 정보
        private ClubList memberClubList; // 사용자가 속한 클럽 목록
        private List<BasicInfo> basicInfoList;
        private boolean hasNext;
        private Long nextCursor;
        private int pageSize;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScopeInfo {
        private BookStoryRequestDTO.BookStoryScope scope; // 현재 범위 (ALL, MY, CLUB)
        private ClubManagementExternalDTO.BasicInfo selectedClub; // 선택된 클럽 정보 (CLUB scope일 때만)
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
        private int likes;

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
        private int likes;

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
    public static class CommentInfo {
        private Long commentId;
        private String content;
        private MemberExternalDTO.BasicInfo authorInfo;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdAt;

        private boolean writtenByMe; // 작성자가 본인인지 여부
        private boolean deleted; // 삭제된 댓글인지 여부
        private List<CommentInfo> replies; // 대댓글 목록
    }
}