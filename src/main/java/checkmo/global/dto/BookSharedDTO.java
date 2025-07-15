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
    public static class BasicInfo {
        private String bookId;          // ISBN을 책 Id로 사용
        private String title;           // 책 제목
        private String author;          // 저자명
        private String coverImageUrl;   // 책 표지 이미지 URL
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailInfo {
        private String bookId;
        private String title;
        private String author;
        private String coverImageUrl;
        private String publisher;       // 출판사
        private String description;     // 책 설명/줄거리
    }
}
