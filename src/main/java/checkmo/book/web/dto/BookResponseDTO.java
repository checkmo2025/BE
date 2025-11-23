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
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookList {
        private List<DetailInfo> detailInfoList;
        private boolean hasNext;
        private Integer currentPage;
    }
}
