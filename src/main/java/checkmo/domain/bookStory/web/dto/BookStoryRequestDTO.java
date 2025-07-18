package checkmo.domain.bookStory.web.dto;

import checkmo.global.dto.BookSharedDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookStoryRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class BookStoryCreateRequestDTO {
        private BookSharedDTO.BookCreateRequestDTO bookInfo; // 책 정보
        private String title;
        private String description;
    }

    @Getter
    @NoArgsConstructor
    public static class BookStoryUpdateRequestDTO {
        private Long bookStoryId;
        private String description;
    }
}
