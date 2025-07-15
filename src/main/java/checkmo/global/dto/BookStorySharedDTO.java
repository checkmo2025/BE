package checkmo.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class BookStorySharedDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookStoryPreviewListDTO {
        private List<BookStoryPreviewDTO> bookStoryPreviews;
        private boolean hasNext;
        private Long nextCursor;
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookStoryPreviewDTO {
        private Long bookStoryId;
        private String bookStoryTitle;
        private BookSharedDTO.BasicInfoDTO bookInfo; // Book 도메인의 공유 DTO
        private int likes;
        private LocalDateTime createdAt;
        private boolean isLiked; // 조회하는 사람의 좋아요 여부
    }
}