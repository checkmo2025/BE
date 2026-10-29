package checkmo.book.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

public class Yes24ApiResponseDTO {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(Boolean success, String errorCode, Data data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(List<Item> items, Integer currentPage, Integer pageSize, Integer totalCount) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(String isbn13, String title, String author, String publisher,
                       String cover, String link, Content contentDetail) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(String bookIntroduction, String bookSummary) {}
}
