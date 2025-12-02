package checkmo.book.web.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AladinApiResponseDTO {

    @Getter
    @NoArgsConstructor
    @JacksonXmlRootElement(localName = "object")
    public static class BookList {
        @JacksonXmlProperty(localName = "item")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<BookItem> items;

        @JacksonXmlProperty(localName = "totalResults")
        private int totalResults;

        @JacksonXmlProperty(localName = "startIndex")
        private int startIndex;

        @JacksonXmlProperty(localName = "itemsPerPage")
        private int itemsPerPage;
    }

    @Getter
    @NoArgsConstructor
    public static class BookItem {
        @JacksonXmlProperty(localName = "title")
        private String title;

        private String author;

        @JacksonXmlProperty(localName = "isbn13")
        private String isbn13;

        @JacksonXmlProperty(localName = "cover")
        private String cover;

        @JacksonXmlProperty(localName = "publisher")
        private String publisher;

        @JacksonXmlProperty(localName = "description")
        private String description;

        @JacksonXmlProperty(localName = "link")
        private String link;

        @JacksonXmlProperty(localName = "author")
        public void setAuthor(String rawAuthor) {
            if (rawAuthor == null || rawAuthor.trim().isEmpty()) {
                this.author = null;
                return;
            }

            this.author = rawAuthor
                    .replaceAll("\\s*\\([^)]*\\)", "")
                    .trim();
        }
    }
}
