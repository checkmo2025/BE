package checkmo.news.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class NewsRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class CreateNews {
        @NotBlank(message = "소식 제목은 필수입니다.")
        @Size(max = 40, message = "소식 제목은 40자 이하로 입력해주세요.")
        private String title;

        @NotBlank(message = "요청자 이메일은 필수입니다.")
        private String requesterEmail;

        @NotBlank(message = "소식 내용은 필수입니다.")
        private String content;

        private String thumbnailUrl;

        private String originalLink;

        @NotNull(message = "게시 시작일은 필수입니다.")
        private LocalDate publishStartAt;

        @NotNull(message = "게시 종료일은 필수입니다.")
        private LocalDate publishEndAt;

        @Size(max = 5, message = "기타 이미지는 최대 5개까지 가능합니다.")
        private List<@NotBlank(message = "이미지 URL은 비어있을 수 없습니다.") String> imageUrls;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateNews {
        @NotBlank(message = "소식 제목은 필수입니다.")
        @Size(max = 40, message = "소식 제목은 40자 이하로 입력해주세요.")
        private String title;

        @NotBlank(message = "요청자 이메일은 필수입니다.")
        private String requesterEmail;

        @NotBlank(message = "소식 내용은 필수입니다.")
        private String content;

        private String thumbnailUrl;

        private String originalLink;

        @NotNull(message = "게시 시작일은 필수입니다.")
        private LocalDate publishStartAt;

        @NotNull(message = "게시 종료일은 필수입니다.")
        private LocalDate publishEndAt;

        @Size(max = 5, message = "기타 이미지는 최대 5개까지 가능합니다.")
        private List<@NotBlank(message = "이미지 URL은 비어있을 수 없습니다.") String> imageUrls;
    }
}