package checkmo.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 다른 도메인에서 책 정보를 참조할 때 사용하는 공유 DTO 클래스
 */
public class BookExternalDTO {

    /**
     * 책 생성 요청 DTO, 새로운 책을 시스템에 등록할 때 사용 ex. 책 이야기 작성 시 책 정보 등록할 때, 책 추천 작성 시 책 정보 등록할 때, 미팅 생성 시 책 정보 등록할 때
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookCreate {
        private String isbn;
        private String title;
        private String author;
        private String imgUrl;
        private String publisher;
        private String description;
    }

    /**
     * 책 기본 정보 DTO 다른 도메인에서 책 정보를 간략히 참조할 때 사용 ex. 책이야기 목록보기, 모임 목록보기
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BasicInfo {
        private String bookId;
        private String title;
        private String author;
        private String imgUrl;
    }

    /**
     * 책 상세 정보 DTO 상세 정보가 필요한 곳에서 사용 ex. 책이야기 상세 페이지, 모임 상세 페이지, 책 추천 상세 페이지 등
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailInfo {
        private String bookId;
        private String title;
        private String author;
        private String imgUrl;
        private String publisher;
        private String description;
    }
}
