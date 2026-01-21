package checkmo.bookStory.web.dto;

import checkmo.book.BookExternalDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookStoryRequestDTO {

    public enum BookStoryScope {
        ALL, MY, FOLLOWING, CLUB, TARGET
    }

    @Getter
    @NoArgsConstructor
    public static class BookStoryCreate {
        private BookExternalDTO.BookCreate bookInfo; // 책 정보

        @NotBlank(message = "책 이야기에 제목을 입력해주세요.")
        private String title;

        @NotBlank(message = "책 이야기에 내용을 입력해주세요.")
        private String description;
    }

    @Getter
    @NoArgsConstructor
    public static class BookStoryUpdate {
        @NotBlank(message = "책 이야기에 수정할 내용을 입력해주세요.")
        private String description;
    }

    @Getter
    @NoArgsConstructor
    public static class CommentCreate {
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        private String content;
    }

    @Getter
    @NoArgsConstructor
    public static class CommentUpdate {
        @NotBlank(message = "수정할 댓글 내용을 입력해주세요.")
        private String content;
    }
}
