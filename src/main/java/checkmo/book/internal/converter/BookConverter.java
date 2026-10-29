package checkmo.book.internal.converter;

import checkmo.book.BookExternalDTO;
import checkmo.book.internal.entity.Book;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.web.dto.AladinApiResponseDTO;
import checkmo.book.web.dto.Yes24ApiResponseDTO;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.util.HtmlUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookConverter {

    public static BookResponseDTO.BookList toYes24BookList(Yes24ApiResponseDTO.Response response) {
        var data = requireYes24Data(response);
        if (data.currentPage() == null || data.currentPage() < 1
                || data.pageSize() == null || data.pageSize() < 1
                || data.totalCount() == null || data.totalCount() < 0) {
            throw new BookException(BookErrorStatus.YES24_API_ERROR);
        }
        if (data.totalCount() == 0 && data.items().isEmpty()) {
            return createEmptyBookListResponse();
        }
        return BookResponseDTO.BookList.builder()
                .detailInfoList(data.items().stream()
                        .filter(item -> item != null && isValidIsbn13(item.isbn13()))
                        .map(BookConverter::toYes24Detail)
                        .toList())
                .currentPage(data.currentPage())
                .totalResults(data.totalCount())
                .hasNext((long) data.currentPage() * data.pageSize() < data.totalCount())
                .build();
    }

    public static DetailInfo toYes24BookDetail(Yes24ApiResponseDTO.Response response, String isbn) {
        var data = requireYes24Data(response);
        // 상품번호나 다른 판본으로 대체하지 않는다. 동일 ISBN의 복수 상품은 공급자 순서로 선택한다.
        return data.items().stream()
                .filter(item -> item != null && isValidIsbn13(item.isbn13()) && item.isbn13().equals(isbn))
                .findFirst()
                .map(BookConverter::toYes24Detail)
                .orElseThrow(() -> new BookException(BookErrorStatus.BOOK_NOT_FOUND));
    }

    private static Yes24ApiResponseDTO.Data requireYes24Data(Yes24ApiResponseDTO.Response response) {
        if (response == null || !Boolean.TRUE.equals(response.success())
                || response.data() == null || response.data().items() == null) {
            throw new BookException(BookErrorStatus.YES24_API_ERROR);
        }
        return response.data();
    }

    private static boolean isValidIsbn13(String isbn) {
        if (isbn == null || !isbn.matches("97[89][0-9]{10}")) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < isbn.length(); i++) {
            sum += (isbn.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
        }
        return sum % 10 == 0;
    }

    private static DetailInfo toYes24Detail(Yes24ApiResponseDTO.Item item) {
        var content = item.contentDetail();
        String description = content == null ? "" : content.bookIntroduction();
        if (content != null && (description == null || description.isBlank())) {
            description = content.bookSummary();
        }
        return DetailInfo.builder()
                .isbn(item.isbn13())
                .title(item.title())
                .author(item.author())
                .publisher(item.publisher())
                .imgUrl(item.cover())
                .link(item.link())
                .description(plainDescription(description))
                .likedByMe(false)
                .build();
    }

    private static String plainDescription(String value) {
        if (value == null) {
            return "";
        }
        return HtmlUtils.htmlUnescape(value)
                .replaceAll("(?is)<(script|style)\\b[^>]*>.*?</\\1\\s*>", " ")
                .replaceAll("(?s)<[^>]*>", " ")
                .replace("\\n", " ")
                .replace('\u00a0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static BookExternalDTO.BasicInfo toBasicInfoDTO(Book book) {
        return BookExternalDTO.BasicInfo.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .imgUrl(book.getImgUrl())
                .build();
    }

    public static BookExternalDTO.DetailInfo toDetailInfoDTO(Book book) {
        return BookExternalDTO.DetailInfo.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .imgUrl(book.getImgUrl())
                .publisher(book.getPublisher())
                .description(book.getDescription())
                .build();
    }

    public static BookResponseDTO.LikedBookInfo toLikedBookInfo(Book book, boolean likedByMe) {
        return BookResponseDTO.LikedBookInfo.builder()
                .isbn(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .imgUrl(book.getImgUrl())
                .likes(book.getLikes())
                .likedByMe(likedByMe)
                .build();
    }

    public static Map<String, BookExternalDTO.BasicInfo> toBasicInfoDTOMap(Map<String, Book> booksMap) {
        return booksMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toBasicInfoDTO(entry.getValue())
                ));
    }

    public static Book toBook(BookExternalDTO.BookCreate request) {
        return Book.builder()
                .id(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .imgUrl(request.getImgUrl())
                .publisher(request.getPublisher())
                .description(request.getDescription())
                .build();
    }

    public static BookExternalDTO.BookCreate toBookCreate(DetailInfo detail) {
        return BookExternalDTO.BookCreate.builder()
                .isbn(detail.getIsbn())
                .title(detail.getTitle())
                .author(detail.getAuthor())
                .imgUrl(detail.getImgUrl())
                .publisher(detail.getPublisher())
                .description(detail.getDescription())
                .build();
    }

    public static BookResponseDTO.BookList toBookList(AladinApiResponseDTO.BookList bookList, int page) {
        if (isInvalidResponse(bookList)) {
            return createEmptyBookListResponse();
        }

        var books = convertItemsToBookList(bookList.getItems());
        boolean hasNext = calculateHasNext(bookList);

        return BookResponseDTO.BookList.builder()
                .detailInfoList(books)
                .hasNext(hasNext)
                .currentPage(page)
                .totalResults(bookList.getTotalResults())
                .build();
    }

    public static DetailInfo toBookInfoDetail(
            AladinApiResponseDTO.BookList bookList
    ) {
        var book = convertItemToDetail(bookList.getItems().getFirst());

        return DetailInfo.builder()
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .imgUrl(book.getImgUrl())
                .publisher(book.getPublisher())
                .description(book.getDescription())
                .link(book.getLink())
                .build();
    }

    private static boolean isInvalidResponse(AladinApiResponseDTO.BookList bookList) {
        return bookList == null || bookList.getItems() == null;
    }

    private static BookResponseDTO.BookList createEmptyBookListResponse() {
        return BookResponseDTO.BookList.builder()
                .detailInfoList(List.of())
                .hasNext(false)
                .currentPage(null)
                .totalResults(0)
                .build();
    }

    private static DetailInfo convertItemToDetail(AladinApiResponseDTO.BookItem item) {
        String description = (item.getDescription() != null) ? item.getDescription() : "";

        String cleanedDescription = HtmlUtils.htmlUnescape(description)
                .replace("\\n", " ")
                .replace("\n", " ")
                .trim();

        String replaceImgUrl = item.getCover().replace("coversum", "cover500");

        return DetailInfo.builder()
                .isbn(item.getIsbn13())
                .title(item.getTitle())
                .author(item.getAuthor())
                .imgUrl(replaceImgUrl)
                .publisher(item.getPublisher())
                .description(cleanedDescription)
                .link(item.getLink())
                .build();
    }

    private static List<DetailInfo> convertItemsToBookList(
            List<AladinApiResponseDTO.BookItem> items
    ) {
        return items.stream()
                .filter(item -> item.getIsbn13() != null && !item.getIsbn13().trim().isEmpty())
                .map(BookConverter::convertItemToDetail)
                .toList();
    }

    private static boolean calculateHasNext(AladinApiResponseDTO.BookList bookList) {
        int totalResults = bookList.getTotalResults();
        int startIndex = bookList.getStartIndex();
        int itemsPerPage = bookList.getItemsPerPage();
        int itemsInThisResponse = bookList.getItems().size();

        int totalItemsFetched = (startIndex - 1) * itemsPerPage + itemsInThisResponse;

        return totalItemsFetched < totalResults;
    }
}
