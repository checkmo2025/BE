package checkmo.domain.club.dto.bookshelf;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookShelfRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class BookReviewDTO {
        @Size(max = 20, message = "한줄평은 20자 이하로 입력해주세요")
        private String description; // 책에 대한 한줄평 내용

        @Size(min = 1, max = 5, message = "평점은 1.0에서 5.0 사이의 값을 입력해주세요")
        private double rate; // 책 평점 (1.0~5.0)
    }
}
