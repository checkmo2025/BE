package checkmo.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookSharedDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookCreateRequestDTO {
        private String isbn;
        private String title;
        private String author;
        private String imgUrl;
        private String publisher;
        private String description;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BasicInfoDTO {
        private String bookId;          // ISBN을 책 Id로 사용
        private String title;           // 책 제목
        private String author;          // 저자명
        private String imgUrl;   // 책 표지 이미지 URL
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailInfoDTO {
        private String bookId;
        private String title;
        private String author;
        private String imgUrl;
        private String publisher;       // 출판사
        private String description;     // 책 설명/줄거리
    }
}
