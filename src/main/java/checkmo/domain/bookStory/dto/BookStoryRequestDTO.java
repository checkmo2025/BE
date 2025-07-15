package checkmo.domain.bookStory.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookStoryRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class BookStoryCreateRequestDTO {
        private BookInfoDTO bookInfo; // 책 정보
        private String title;
        private String description;
    }

    @Getter
    @NoArgsConstructor
    public static class BookInfoDTO {
        private String isbn; // ISBN 번호 (책의 고유 ID)
        private String title; // 책 제목
        private String author; // 저자
        private String imgUrl; // 책 이미지 URL
        private String publisher; // 출판사
        private String description; // 책 설명
    }

    @Getter
    @NoArgsConstructor
    public static class BookStoryUpdateRequestDTO {
        private Long bookStoryId;
        private String description;
    }
}
