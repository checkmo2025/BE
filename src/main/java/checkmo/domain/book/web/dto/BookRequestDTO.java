package checkmo.domain.book.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Aladin2BookDTO {
        private String isbn; // ISBN 번호
        private String title; // 책 제목
        private String author; // 저자
        private String imgUrl; // 책 이미지 URL
        private String publisher; // 출판사
        private String description; // 책 설명
    }
}
