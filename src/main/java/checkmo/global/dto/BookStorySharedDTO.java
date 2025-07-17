package checkmo.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 북스토리 도메인과 관련된 공유 DTO 클래스
 * 다른 도메인에서 북스토리 정보를 참조할 때 사용
 */
public class BookStorySharedDTO {

    /**
     * 북스토리 미리보기 목록 응답 DTO
     * 외부 도메인에서 북스토리 특정 개수만큼 조회할 때 사용
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookStoryPreviewListDTO {
        private List<BookStoryPreviewDTO> bookStoryPreviews;
    }

    /**
     * 북스토리 미리보기 정보 DTO
     * 북스토리의 기본 정보와 관련 책 정보, 좋아요 수 등을 포함
     * 피드나 목록 화면에서 북스토리를 간략히 표시할 때 사용
     */
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