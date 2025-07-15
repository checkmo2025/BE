package checkmo.domain.bookStory.dto;

import checkmo.global.dto.BookSharedDTO;
import checkmo.global.dto.MemberSharedDTO;
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
    public static class BookStoryListResponse {
        private List<BookStoryResponse> bookStoryResponses;
        private boolean hasNext;        // 다음 페이지 존재 여부
        private Long nextCursor;        // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize;           // 현재 페이지 크기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookStoryResponse {
        private String bookStoryId;
        private BookSharedDTO.BasicInfo bookInfo; // 책 정보 - 공용 DTO 사용
        private MemberSharedDTO.WithFollowStatus authorInfo; // 작성자 정보 - 공용 DTO 사용
        private String bookStoryTitle;
        private String description;
        private int likes;
        private boolean isLiked;
        private LocalDateTime createdAt;
    }
}
