package checkmo.domain.book.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class BookResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookInfoDetailResponse {
        private String isbn; // ISBN 번호
        private String title; // 책 제목
        private String author; // 저자
        private String imgUrl; // 책 이미지 URL
        private String publisher; // 출판사
        private String description; // 책 설명
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookListResponse {
        List<BookInfoDetailResponse> bookInfoDetailResponseList; // 책 정보 목록
        private boolean hasNext;        // 다음 페이지 존재 여부
        private Integer currentPage;           // 현재 페이지 번호
    }
}
