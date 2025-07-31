package checkmo.domain.club.web.dto.bookshelf;

import checkmo.domain.club.validation.ValidRate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookShelfRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class BookReviewDTO {
        @NotBlank(message = "한줄평은 필수 입력입니다.")
        @Size(max = 20, message = "한줄평은 20자 이하로 입력해주세요.")
        private String description; // 책에 대한 한줄평 내용

        @NotNull(message = "평점은 null이 될 수 없습니다.")
        @ValidRate
        private Double rate; // 책에 대한 평점
    }

    @Getter
    @NoArgsConstructor
    public static class TopicDTO {
        @NotBlank(message = "발제는 필수 입력입니다.")
        private String description; // 토픽 내용
    }
}
