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

    // =====================================================
    // BookResponseDTO → BookSharedDTO 변환
    // =====================================================

    /**
     * BookResponseDTO → BasicInfoDTO 변환 (기본 정보만)
     */
    public static BookSharedDTO.BasicInfoDTO fromBookDTOToBasicInfoDTO(
            BookResponseDTO.BookInfoDetailResponse book
    ) {
        return BookSharedDTO.BasicInfoDTO.builder()
                .bookId(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .imgUrl(book.getImgUrl())
                .build();
    }

    /**
     * BookResponseDTO → DetailInfoDTO 변환 (상세 정보 포함)
     */
    public static BookSharedDTO.DetailInfoDTO fromBookDTOToDetailInfoDTO(
            BookResponseDTO.BookInfoDetailResponse book
    ) {
        return BookSharedDTO.DetailInfoDTO.builder()
                .bookId(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .imgUrl(book.getImgUrl())
                .publisher(book.getPublisher())
                .description(book.getDescription())
                .build();
    }

    // =====================================================
    // Entity ↔ DTO 변환
    // =====================================================

    /**
     * Book 엔티티 → BookResponseDTO 변환
     */
    public static BookResponseDTO.BookInfoDetailResponse fromBook(Book book) {
        return BookResponseDTO.BookInfoDetailResponse.builder()
                .isbn(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .imgUrl(book.getImgUrl())
                .publisher(book.getPublisher())
                .description(book.getDescription())
                .build();
    }

    /**
     * BookCreateRequestDTO → Book 엔티티 변환
     */
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

    // =====================================================
    // 알라딘 API 응답 변환
    // =====================================================

    /**
     * 알라딘 API 책 아이템 → BookResponseDTO 변환
     */
    public static BookResponseDTO.BookInfoDetailResponse fromAladinBookItem(
            AladinApiResponseDTO.AladinBookItem item
    ) {
        String decodedDescription = HtmlUtils.htmlUnescape(item.getDescription());

        return BookResponseDTO.BookInfoDetailResponse.builder()
                .isbn(item.getIsbn13())
                .title(item.getTitle())
                .author(item.getAuthor())
                .imgUrl(item.getCover())
                .publisher(item.getPublisher())
                .description(decodedDescription)
                .build();
    }

    /**
     * 알라딘 API 응답 → BookListResponseDTO 변환
     */
    public static BookResponseDTO.BookListResponse fromAladinApiResponse(
            AladinApiResponseDTO.AladinApiResponse aladinApiResponse,
            int page
    ) {
        if (isInvalidResponse(aladinApiResponse)) {
            return createEmptyBookListResponse();
        }

        var books = convertToBookList(aladinApiResponse.getItems());
        boolean hasNext = calculateHasNext(aladinApiResponse);

        return BookResponseDTO.BookListResponse.builder()
                .bookInfoDetailResponseList(books)
                .hasNext(hasNext)
                .currentPage(page)
                .build();
    }

    /**
     * 알라딘 API 응답 → BookInfoDetailResponseDTO 변환
     */
    public static BookResponseDTO.BookInfoDetailResponse fromAladinApiResponse(
            AladinApiResponseDTO.AladinApiResponse aladinApiResponse
    ) {
        var book = fromAladinBookItem(aladinApiResponse.getItems().getFirst());

        return BookResponseDTO.BookInfoDetailResponse.builder()
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .imgUrl(book.getImgUrl())
                .publisher(book.getPublisher())
                .description(book.getDescription())
                .build();
    }

    // =====================================================
    // Private Methods
    // =====================================================

    /**
     * 알라딘 API 응답 유효성 검사
     */
    private static boolean isInvalidResponse(AladinApiResponseDTO.AladinApiResponse aladinApiResponse) {
        return aladinApiResponse == null || aladinApiResponse.getItems() == null;
    }

    /**
     * 빈 BookListResponseDTO 생성
     */
    private static BookResponseDTO.BookListResponse createEmptyBookListResponse() {
        return BookResponseDTO.BookListResponse.builder()
                .bookInfoDetailResponseList(List.of())
                .hasNext(false)
                .currentPage(null)
                .build();
    }

    /**
     * 알라딘 API 아이템 리스트 → BookResponseDTO 리스트 변환
     */
    private static List<BookResponseDTO.BookInfoDetailResponse> convertToBookList(
            List<AladinApiResponseDTO.AladinBookItem> items
    ) {
        return items.stream()
                .map(BookConverter::fromAladinBookItem)
                .toList();
    }

    /**
     * 다음 페이지 존재 여부 계산
     */
    private static boolean calculateHasNext(AladinApiResponseDTO.AladinApiResponse aladinApiResponse) {
        int totalResults = aladinApiResponse.getTotalResults();
        int startIndex = aladinApiResponse.getStartIndex();
        int itemsPerPage = aladinApiResponse.getItemsPerPage();
        int itemsInThisResponse = aladinApiResponse.getItems().size();

        int totalItemsFetched = (startIndex - 1) * itemsPerPage + itemsInThisResponse;

        return totalItemsFetched < totalResults;
    }
}
