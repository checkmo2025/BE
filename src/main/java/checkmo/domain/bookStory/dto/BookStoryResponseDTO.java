package checkmo.domain.bookStory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class BookStoryResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookStoryResponse {
        private String bookStoryId;
        private String bookTitle;
        private String bookImgUrl;
        private AuthorInfo authorInfo;
        private String bookStoryTitle;
        private String description;
        private int likes;
        private boolean isLiked;
        private LocalDateTime createdAt;

        @Getter
        @Builder
        public static class AuthorInfo {
            // 여기서 말하는 Author는 로그인한 사용자가 아닌 책 이야기 작성자!!!!
            private String nickname;
            private String profileImgUrl;
            private boolean isFollowing; //로그인한 회원이 책 이야기 작성자를 팔로우하고 있는지 여부
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookStoryListResponse {
        private List<BookStoryResponse> bookStoryResponses;
        private boolean hasNext;        // 다음 페이지 존재 여부
        private Long nextCursor;        // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize;           // 현재 페이지 크기
    }
}
