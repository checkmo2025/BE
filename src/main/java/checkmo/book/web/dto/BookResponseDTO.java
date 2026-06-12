package checkmo.book.web.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailInfo {
        private String isbn;
        private String title;
        private String author;
        private String imgUrl;
        private String publisher;
        private String description;
        private String link;
        private boolean likedByMe;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookList {
        private List<DetailInfo> detailInfoList;
        private boolean hasNext;
        private Integer currentPage;
        private int totalResults;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LikeResult {
        private String isbn;
        private boolean liked;
        private int likes;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LikedBookInfo {
        private String isbn;
        private String title;
        private String author;
        private String imgUrl;
        private int likes;
        private boolean likedByMe;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LikedBookList {
        private List<LikedBookInfo> books;
        private boolean hasNext;
        private Long nextCursor;
    }
}
