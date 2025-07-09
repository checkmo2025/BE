package checkmo.domain.book.dto;

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
    public static class BookInfoDetailResponseDTO {
        private String id; // ISBN 번호
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
    public static class BookListResponseDTO {
        List<BookInfoDetailResponseDTO> bookInfoDetailResponseDTOs; // 책 정보 목록
        private boolean hasNext;        // 다음 페이지 존재 여부
        private Long nextCursor;        // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize;           // 현재 페이지 크기
    }

}
