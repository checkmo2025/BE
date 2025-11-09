package checkmo.bookStory;

import checkmo.book.BookSharedDTO;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.clubManagement.ClubManagementSharedDTO;
import checkmo.member.MemberSharedDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookStorySharedDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookStoryListResponse {
        private ScopeInfo scopeInfo;    // 현재 선택된 범위 정보
        private ClubManagementSharedDTO.MyClubList memberClubList; // 사용자가 속한 클럽 목록
        private List<BookStoryResponse> bookStoryResponses;
        private boolean hasNext;        // 다음 페이지 존재 여부
        private Long nextCursor;        // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize;           // 현재 페이지 크기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScopeInfo {
        private BookStoryRequestDTO.BookStoryScope scope; // 현재 범위 (ALL, MY, CLUB)
        private ClubManagementSharedDTO.MyClubInfo selectedClub; // 선택된 클럽 정보 (CLUB scope일 때만)
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookStoryResponse {
        private Long bookStoryId;
        private BookSharedDTO.BasicInfo bookInfo; // 책 정보 - 공용 DTO 사용
        private MemberSharedDTO.WithFollowStatus authorInfo; // 작성자 정보 - 공용 DTO 사용
        private String bookStoryTitle;
        private String description;
        private int likes;

        private boolean likedByMe; // 내가 좋아요를 눌렀는지 여부 (true: 눌렀음, false: 안누름)

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdAt;

        private boolean writtenByMe; // 작성자가 본인인지 여부 (true: 본인, false: 타인)
        private int commentCount; // 댓글 개수
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookStoryDetailResponse {
        private Long bookStoryId;
        private BookSharedDTO.BasicInfo bookInfo; // 책 정보 - 공용 DTO 사용
        private MemberSharedDTO.WithFollowStatus authorInfo; // 작성자 정보 - 공용 DTO 사용
        private String bookStoryTitle;
        private String description;
        private int likes;

        private boolean likedByMe; // 내가 좋아요를 눌렀는지 여부 (true: 눌렀음, false: 안누름)

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdAt;

        private boolean writtenByMe; // 작성자가 본인인지 여부 (true: 본인, false: 타인)
        private int commentCount; // 댓글 전체 개수 (대댓글 포함)

        private List<CommentResponse> comments; // 댓글 목록
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommentResponse {
        private Long commentId;
        private String content;
        private MemberSharedDTO.BasicInfo authorInfo; // 작성자 정보

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdAt;

        private boolean writtenByMe; // 작성자가 본인인지 여부
        private List<CommentResponse> replies; // 대댓글 목록
    }
}
