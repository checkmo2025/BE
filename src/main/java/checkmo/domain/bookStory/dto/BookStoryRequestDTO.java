package checkmo.domain.bookStory.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookStoryRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class BookStoryCreateRequestDTO {
        private String bookId;
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
