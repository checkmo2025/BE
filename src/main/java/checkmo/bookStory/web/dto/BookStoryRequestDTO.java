package checkmo.bookStory.web.dto;

import checkmo.bookStory.internal.entity.BookStoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookStoryRequestDTO {

    public enum BookStoryScope {
        ALL, MY, FOLLOWING, CLUB, TARGET
    }

    @Getter
    @NoArgsConstructor
    public static class BookStoryCreate {
        @NotBlank(message = "ISBN은 필수입니다.")
        @Pattern(regexp = "^\\d{13}$", message = "ISBN은 13자리 숫자여야 합니다.")
        private String isbn;

        @NotBlank(message = "책 이야기에 제목을 입력해주세요.")
        @Size(max = 100, message = "책 이야기 제목은 100자 이하로 입력해주세요.")
        private String title;

        @Size(max = 5000, message = "책 이야기 본문은 5000자 이하로 입력해주세요.")
        private String description;

        private BookStoryStatus status;
    }

    @Getter
    @NoArgsConstructor
    public static class BookStoryUpdate {
        @Pattern(regexp = "^\\d{13}$", message = "ISBN은 13자리 숫자여야 합니다.")
        private String isbn;

        @NotBlank(message = "책 이야기에 수정할 제목을 입력해주세요.")
        @Size(max = 100, message = "책 이야기 제목은 100자 이하로 입력해주세요.")
        private String title;

        @Size(max = 5000, message = "책 이야기 본문은 5000자 이하로 입력해주세요.")
        private String description;

        private BookStoryStatus status;
    }

    @Getter
    @NoArgsConstructor
    public static class CommentCreate {
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(max = 300, message = "댓글은 300자 이하로 입력해주세요.")
        private String content;
    }

    @Getter
    @NoArgsConstructor
    public static class CommentUpdate {
        @NotBlank(message = "수정할 댓글 내용을 입력해주세요.")
        @Size(max = 300, message = "댓글은 300자 이하로 입력해주세요.")
        private String content;
    }
}
