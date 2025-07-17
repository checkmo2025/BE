package checkmo.domain.book.converter;

import checkmo.domain.book.entity.Book;
import checkmo.domain.book.web.dto.AladinApiResponseDTO;
import checkmo.domain.book.web.dto.BookResponseDTO;
import checkmo.global.dto.BookSharedDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookConverter {

    public static Book fromBookCreateRequestDTO(
            BookSharedDTO.BookCreateRequestDTO request
    ) {
        return Book.builder()
                .id(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .imgUrl(request.getImgUrl())
                .publisher(request.getPublisher())
                .description(request.getDescription())
                .build();
    }

    public static BookResponseDTO.BookInfoDetailResponseDTO fromAladinBookItem(
            AladinApiResponseDTO.AladinBookItem item
    ) {
        String decodedDescription = HtmlUtils.htmlUnescape(item.getDescription());

        return BookResponseDTO.BookInfoDetailResponseDTO.builder()
                .isbn(item.getIsbn13())
                .title(item.getTitle())
                .author(item.getAuthor())
                .imgUrl(item.getCover())
                .publisher(item.getPublisher())
                .description(decodedDescription)
                .build();
    }

    public static BookResponseDTO.BookListResponseDTO fromAladinApiResponse(
            AladinApiResponseDTO.AladinApiResponse aladinApiResponse,
            int page
    ) {
        if (isInvalidResponse(aladinApiResponse)) {
            return createEmptyBookListResponse();
        }

        List<BookResponseDTO.BookInfoDetailResponseDTO> books = convertToBookList(aladinApiResponse.getItems());
        boolean hasNext = calculateHasNext(aladinApiResponse);

        return BookResponseDTO.BookListResponseDTO.builder()
                .bookInfoDetailResponseDTOs(books)
                .hasNext(hasNext)
                .currentPage(page)
                .build();
    }

    private static boolean isInvalidResponse(AladinApiResponseDTO.AladinApiResponse aladinApiResponse) {
        return aladinApiResponse == null || aladinApiResponse.getItems() == null;
    }

    private static BookResponseDTO.BookListResponseDTO createEmptyBookListResponse() {
        return BookResponseDTO.BookListResponseDTO.builder()
                .bookInfoDetailResponseDTOs(List.of())
                .hasNext(false)
                .currentPage(null)
                .build();
    }

    private static List<BookResponseDTO.BookInfoDetailResponseDTO> convertToBookList(
            List<AladinApiResponseDTO.AladinBookItem> items) {
        return items.stream()
                .map(BookConverter::fromAladinBookItem)
                .toList();
    }

    private static boolean calculateHasNext(AladinApiResponseDTO.AladinApiResponse aladinApiResponse) {
        int totalResults = aladinApiResponse.getTotalResults();
        int startIndex = aladinApiResponse.getStartIndex();
        int itemsPerPage = aladinApiResponse.getItemsPerPage();
        int itemsInThisResponse = aladinApiResponse.getItems().size();

        int totalItemsFetched = (startIndex - 1) * itemsPerPage + itemsInThisResponse;

        return totalItemsFetched < totalResults;
    }
}
