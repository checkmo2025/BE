package checkmo.clubMeeting.web.dto.bookshelf;

import checkmo.book.BookExternalDTO;
import checkmo.clubMeeting.internal.validation.validRate.ValidRate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookShelfRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class BookShelfCreate {
        @Size(max = 12, message = "독서모임 제목은 12자 이하로 입력해주세요.")
        private String title;

        private LocalDateTime meetingTime;

        @Size(max = 12, message = "독서모임 장소는 12자 이하로 입력해주세요.")
        private String location;

        @Min(value = 1, message = "기수는 1 이상의 정수여야 합니다.")
        private Integer generation;

        @Size(max = 4, message = "태그는 최대 4글자까지 입력 가능합니다.")
        private String tag;

        @Valid
        @NotNull(message = "책 정보는 null이 될 수 없습니다.")
        private BookExternalDTO.BookCreate bookInfo;
    }

    @Getter
    @NoArgsConstructor
    public static class BookShelfUpdate {
        @Size(max = 12, message = "독서모임 제목은 12자 이하로 입력해주세요.")
        private String title;

        private LocalDateTime meetingTime;

        @Size(max = 12, message = "독서모임 장소는 12자 이하로 입력해주세요.")
        private String location;

        @Min(value = 1, message = "기수는 1 이상의 정수여야 합니다.")
        private Integer generation;

        @Size(max = 4, message = "태그는 최대 4글자까지 입력 가능합니다.")
        private String tag;
    }

    @Getter
    @NoArgsConstructor
    public static class BookReviewCreate {
        @NotBlank(message = "한줄평은 필수 입력입니다.")
        @Size(max = 300, message = "한줄평은 300자 이하로 입력해주세요.")
        private String description;

        @NotNull(message = "평점은 null이 될 수 없습니다.")
        @ValidRate
        private Double rate;
    }

    @Getter
    @NoArgsConstructor
    public static class TopicCreate {
        @NotBlank(message = "발제는 필수 입력입니다.")
        @Size(max = 300, message = "발제는 300자 이하로 입력해주세요.")
        private String description;
    }
}
